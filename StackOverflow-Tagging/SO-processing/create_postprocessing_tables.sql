DROP TABLE IF EXISTS pp_context;
CREATE TABLE pp_context (
	pqn text,
	clt_name text,
	tid text,
	du text,
	pos text,
	context text
);

DROP TABLE IF EXISTS pp_no_stop_context;
CREATE TABLE pp_no_stop_context (
	pqn text,
	clt_name text,
	tid text,
	du text,
	pos text,
	no_stops_context text
);


