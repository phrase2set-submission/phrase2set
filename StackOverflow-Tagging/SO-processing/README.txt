Extracting the surrounding context of code-like terms identified and saved in the database
------------------------------------------------------------------------------------------

This document describes the steps needed to extract the surrounding context of code-like terms identified and saved to the Stack Overflow
posts database. The extracted context is then saved to the database, with one database table preserving the original context of the 
code-like terms and another table storing the context of the code-like terms after the context is stripped of English stop words.

Prerequisites:

- Stack Overflow (SO) database containing 
	1) The 'posts' table containing the stack overflow question and answer posts
	2) The 'clt' table containing the code-like terms identified in the SO posts.
	3) The following files located in the same directory
		- android.conf
		- context_finder.pl
		- create_postprocessing_tables.sql
		- delete_words_in_contexts.pl
		- html.pm
		- remove_stop_words.pm

	*IMPORTANT
	The 'pos' column values in the clt table represent the starting character position of the code-like term after the post body has been 
	cleaned up with the html->strip_html($) subroutine found in the html.pm custom module. The 'pos' column value will not match the start
	of the code-like term in the post unless the post in stripped of html tags and elements through html->strip_html($)

	Also note that the 'pos' column value that is saved to the post-processing context tables corresponds to the position of the code-like 
	term in the original post whose ID is specified by the 'du' column value (after the post body is cleaned with html->strip_html($)). 


What to execute in order to save the context information:

1) 	Create the necessary database tables to store the context information to the database holding the 'posts' and 'clt' table.
	This can be done by running the create_postprocessing_tables.sql script on the database.

2) 	Save the original surrounding context of each code-like term in the database, post by post, to the pp_context table. 
	This can be done by running the context_finder.pl script. 
	Make sure the android.conf file specifies the correct database credentials

2) 	Strip the original surrounding context of English stop words and save the result to the pp_no_context table.
	This can be done by running the delete_words_in_contexts.pl script.
	Make sure the android.conf file specifies the correct database credentials
