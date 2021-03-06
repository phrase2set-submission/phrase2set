#!/usr/bin/perl

use warnings;
use strict;

use DBI;
use Config::General;

use remove_stop_words; # Local module

my $config_path = shift @ARGV;

if (!defined $config_path) {
	$config_path = 'android.conf';
}
die "Config file \'$config_path\' does not exist"
	unless (-e $config_path);

my %config =  Config::General::ParseConfig($config_path);

sub clean_text($){
	my($text) = @_;
	
	# Remove code tags from text
	$text =~ s/<code>|<\/code>/ /g;
	return $text;
}

# Prepare database statements
my $dbh_ref = DBI->connect("dbi:Pg:database=$config{db_name}", '', '', {AutoCommit => 0});
my $get_context = $dbh_ref->prepare(q{select pqn, clt_name, tid, du, pos, context from pp_context});
my $insert_no_stop_context = $dbh_ref->prepare(qq{insert into pp_no_stop_context(pqn, clt_name, tid, du, pos, no_stops_context) values(?,?,?,?,?,?)});

# Buffer variables for committing database inserts in chunks.
my $buffer_capacity = 10000; # Number of inserts performed before the changes to the database are committed
my $buffer_size = 0;

# Load the contents of the context database
$get_context->execute;
while (my($pqn, $clt_name, $tid, $du, $pos, $context) = $get_context->fetchrow_array) {
	# Remove the code tags and the stop words from the context
	$context = clean_text($context);
	my $no_stop_word_context = remove_stop_words->remove_stop_words($context);
	$insert_no_stop_context->execute($pqn, $clt_name, $tid, $du, $pos, $no_stop_word_context); 

	# increment the buffer counter and flush/commit the database connection if the buffer capacity is reached. 
	# This is done so that data is not lost if the script crashes unexpectedly over it's large run time.
	++$buffer_size;
	if($buffer_size >= $buffer_capacity){
		$dbh_ref->commit;
		$buffer_size = 0;
	}
}

# Cleanup
$get_context->finish;
$insert_no_stop_context->finish;
$dbh_ref->commit;
$dbh_ref->disconnect;
