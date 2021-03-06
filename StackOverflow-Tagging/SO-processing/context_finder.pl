#!/usr/bin/perl

use warnings;
use strict;

use DBI;
use Cwd; # Core perl module
use Regexp::Common; 

use Config::General;
use HTML::Entities;
use Encode qw(decode encode); # Core perl module
use html; # Local module

# Identifiers for code tags
my $START_CODE = 'prxh_START_SNIPPET'; # identifier for <code> 
my $END_CODE = 'prxh_END_SNIPPET'; # identifier for </code>
my $replacement_char = '~'; # used to replace code-like terms, code blocks, and parameter lists

my $config_path = shift @ARGV;

if (!defined $config_path) {
	$config_path = 'android.conf';
}
die "Config file \'$config_path\' does not exist"
	unless (-e $config_path);

my %config =  Config::General::ParseConfig($config_path);

# Prepare database statements
my $dbh_ref = DBI->connect("dbi:Pg:database=$config{db_name}", '', '', {AutoCommit => 0});

my $get_du = $dbh_ref->prepare(q{select parentid, id, title, body, creationdate from posts LIMIT 1000000}); # Set the post limit as desired
my $get_pos_len = $dbh_ref->prepare(qq{select pqn, simple, kind, pos, length(simple) as len from clt where trust = 0 and du = ?  order by pos});
my $insert_context = $dbh_ref->prepare(qq{insert into pp_context(pqn, clt_name, tid, du, pos, context) values(?,?,?,?,?,?)});

# Buffer variables for commiting database inserts in chunks.
my $buffer_capacity = 10000; # Number of inserts performed before the changes to the database are committed
my $buffer_size = 0;

sub clean_text($){
	my($text) = @_;

	# Remove all instances of the code-like term, code block, and parameter replacement character
	$text =~ s/$replacement_char+/ /g;

	# Clean text of entities that were missed by html->strip_html
	$text =~ s/&amp;/&/g; # convert the &amp; entity first in order to find other entities ex. "&amp;lt;" to "&lt;" to "<"
	$text =~ s/&\.xA;/ /g; # Get rid of &.xA; characters in the text
	$text =~ s/&quot;/"/g; # replace &quot; characters in the text with '"'
	$text =~ s/&lt;/</g; # replace &amp;lt; characters in the text with '<'
	$text =~ s/&gt;/>/g; # replace &amp;gt; characters in the text with '>'
	$text =~ tr/\n\r\f\t / /s; # Get rid of excess whitespaces.
	$text = encode('UTF-8', $text, Encode::FB_DEFAULT); # Encode string to avoid DB crashes upon attempting to insert a non UTF-8 character
	return $text;
}

sub compare_current_context_to_previous_context($$$$$$$$$$){
	my ($pqn, $simple, $tid, $du, $pos, $curr_context, $prev_context, $prev_pqn_array, $prev_clt_array, $prev_pos_array) = @_;

	# If the context extracted for the current code-like term being considered is not equal to the context extracted for the last code-like 
	# term, insert all the code-like terms sharing the previous context into the database. 
	if($curr_context ne $prev_context){
		# Save the context to the database
		save_context(\@$prev_pqn_array, \@$prev_clt_array, \@$prev_pos_array, $tid, $du, $prev_context);

		# Reset previous context to be equal to the current context to catch any following code-like terms with an identical context
		$prev_context = $curr_context;
		# Empty the arrays and clear the arrays 
		@$prev_pqn_array = ();
		@$prev_clt_array = ();
		@$prev_pos_array = ();	
	}

	# Add the current term and its position to the respective lists to keep track 
	# of the names and positions for code-like terms with the same context
	push @$prev_pqn_array, $pqn;
	push @$prev_clt_array, $simple;
	push @$prev_pos_array, "$pos"; # Save position as a string instead of an integer to ensure no complications with concatenation	

	# Return the new current context, which is either the previous context or the current one passed into this subroutine if the two were different
	return $prev_context;
}

sub save_context($$$$$$){
	my ($pqn_array, $clt_array, $pos_array, $tid, $du, $context) = @_;
	
	# Only save the context if there is at least one code element associated with it
	#if(length(@$pqn_array) > 0 and length(@$clt_array) > 0 and length(@$pos_array) > 0){
	# Join the code-like terms with the same context into a single string. Do the same for their positions.
	my $context_pqns = join(", ", @$pqn_array);
	my $context_clts = join(", ", @$clt_array);
	my $context_positions = join(", ", @$pos_array);

	# Save the concatenated code-like terms, positions, and partially qualified names of the context to the database
	$insert_context->execute($context_pqns, $context_clts, $tid, $du, $context_positions, $context);	

	# increment the buffer counter and flush/commit the database connection if the buffer capacity is reached. 
	# This is done so that data is not lost if the script crashes unexpectedly over it's large run time.
	++$buffer_size;
	if($buffer_size >= $buffer_capacity){
		$dbh_ref->commit;
		$buffer_size = 0;
	}
	#}	
}

# get posts from database
$get_du->execute or die "Can't get doc units from db ", $dbh_ref->errstr;

# Find surrounding words of each code element found in each post
while ( my($tid, $du, $title, $content, $creationdate) = $get_du ->fetchrow_array) {
    if(defined $title) {
        $content = $title . ' ' . $content;
    } 
	
	# Positions in the database were originally determined in the content body after html->strip_html was used to clean the body text.
	# Use the same function to get to the code-like term with the position stored in the database.
    my $stripped_content = html->strip_html($content); 

	# Variables used to hold the start position and size of the substring to extract around the code-like term
	my $startp;
	my $chars_before = 200; # how many characters before the code-like term to include in the context string
	my $chars_after = 200; # how many characters after the code-like term to include in the context string
	
	# Replace all identified code-like terms with placeholder strings that can be identified and removed during context extraction.
	$get_pos_len->execute($du) or die "Can't get clts from db ", $dbh_ref->errstr;
	my $clt_stripped_content = $stripped_content;
	my $clt_in_post = 0;
  	while ( my($pqn, $simple, $kind, $pos, $len) = $get_pos_len->fetchrow_array) {
		# Replace the code-like term in the post with the substitution string. The substitution string is of the same size as the 
		# original code-like term so that the positions stored in the database for each code-like term remains correct.
		substr($clt_stripped_content, $pos, $len, $replacement_char x $len); # String multiplication ex. 'a' x 4 = 'aaaa'
		
		# Remove the dot identifier following code elemen that are classes or variables if present. 
		pos($clt_stripped_content) = $pos + $len; # Set Regex start position to the character that immediately follows the code-like term
		if($kind eq 'type' or $kind eq 'variable'){
			# Replace "." in "clt.field".
 			# \G modifier makes regex match start from the current regex position of the string (can be set by pos(string)).
			$clt_stripped_content =~ /\G(\.)\w/s; # No chance for following code element to have been replaced due to ascending position order
			if(defined $1){	
				substr($clt_stripped_content, $pos + $len, length($1) , $replacement_char x length($1))
			}
		}
		# Remove any parameter list parentheses that follow the code element regardless of its kind.
		pos($clt_stripped_content) = $pos + $len; # Set Regex start position to the character that immediately follows the code-like term
 		# Replace "(...)" in "clt(...)" and "(...).field" in "clt(...).field" with non-greedy match for all characters inside parentheses.
		# The 's' modifiers allows the '.' regex symbol to match any character including newlines.
		$clt_stripped_content =~ /\G(\(.*?\))(?:(\.)\w)?/s; # No chance for following code element to have been replaced due to ascending position order
		if(defined $1){
			if(defined $2){
				# Remove the extra period after the parentheses in addition to removing the parentheses
				substr($clt_stripped_content, $pos + $len, length($1) + length($2) , $replacement_char x (length($1) + length($2)));
				$clt_in_post = 1;
			}
			else{
				# Remove the parentheses		
				substr($clt_stripped_content, $pos + $len, length($1), $replacement_char x length($1));
			}
		}
	}
	
	# Substitute code blocks with a replacement string of equal length.
	# Regex Modifiers: 	g=all instances, x=allows whitespaces in regex, m=multiline, s=lets "." match any character including newline
	# 					e=evaluate right hand side as an expression
	$clt_stripped_content =~ s/($START_CODE.*?$END_CODE)/$replacement_char x length($1)/gexms;
	
	# Have a variable store the previous context to keep track of when the extracted context is different from the previous one,
	# as well as variables to hold all the code-like terms and positions found with the previous context
	my $previous_context; 
	my @pqns_with_same_context;
	my @clts_with_same_context;
	my @positions_with_same_context;
	
	# Find the text surrounding each code-like term
 	$get_pos_len->execute($du) or die "Can't get clts from db ", $dbh_ref->errstr;
    while ( my($pqn, $simple, $kind, $pos, $len) = $get_pos_len->fetchrow_array) {
		my $sc_copy = $clt_stripped_content;

		# substr: first parameter is original string, second is start position, third is length of substring.
		# It's okay to have the length exceed the string, but the exact desired length is specified for clarity
		my $left_hand_text = substr($sc_copy, 0, $pos); # Regex positions start from zero for the first character
		my $right_hand_text = substr($sc_copy, $pos + $len, length($sc_copy) - ($pos + $len)); 

		# Remove the junk entities and replacement characters from the extracted text
		my $cleaned_left_hand_text = clean_text($left_hand_text);
		my $cleaned_right_hand_text = clean_text($right_hand_text);

		# Limit the number fo characters extracted from the cleaned text to be part of the context.
		# Also remove the first and last word from the surrounding text unless it is certain from the comparison of the string size to the maximum
		# number of extracted characters that the leading(for lfs) or trailing(for rhs) sequence of non-space characters are whole words.
		if(length($cleaned_left_hand_text) > $chars_before){
			# Keep only the text found a certain distance behind the code-like term
			$cleaned_left_hand_text = substr($cleaned_left_hand_text, length($cleaned_left_hand_text) - $chars_before);
			$cleaned_left_hand_text =~ s/^[^\s]*\s+//; # get rid of first word and/or leading whitespaces in the string
			$cleaned_left_hand_text =~ s/\s+$//; # Strip trailing whitespaces
		}
		if(length($cleaned_right_hand_text) > $chars_after){
			# Keep only the text found a certain distance ahead of the code-like term
			$cleaned_right_hand_text = substr($cleaned_right_hand_text, 0, $chars_after);
			$cleaned_right_hand_text =~ s/\s+[^\s]+$//; # get rid of last word in the string
			$cleaned_right_hand_text =~ s/^\s+//; # Strip leading whitespaces
		}
		# Concatenate the two sides of surrounding text to form the full context
		my $cleaned_surrounding_text = $cleaned_left_hand_text." ".$cleaned_right_hand_text;

		# Set the thread ID to be equal to the document unit ID if the thread ID is not defined. An unidentified
		# thread ID means the post is a question post, with the same thread ID and document ID.
		if(!defined $tid){
			$tid = $du;
		}
		
		# If this is the first code-like term in the post, set the previous context to the first context
		if(not defined $previous_context){
		 	$previous_context =  $cleaned_surrounding_text;
		}	
		
		# Add the code-like term and position to the list of code-like terms and positions sharing the same context
		# if the current context matches the previous one being shared. Otherwise save the previous context 
		# information to the database.
		$previous_context = compare_current_context_to_previous_context($pqn, $simple, $tid, $du, $pos, $cleaned_surrounding_text, $previous_context, 
																		\@pqns_with_same_context, \@clts_with_same_context, \@positions_with_same_context)
    }
	# Save the last context to the database
	if(defined $previous_context){ # If the context is defined, then there was at least one code element found with the post
		save_context(\@pqns_with_same_context, \@clts_with_same_context, \@positions_with_same_context, $tid, $du, $previous_context);
	}
}

# Cleanup
$get_du->finish;
$insert_context->finish;
$dbh_ref->commit;
$dbh_ref->disconnect;
