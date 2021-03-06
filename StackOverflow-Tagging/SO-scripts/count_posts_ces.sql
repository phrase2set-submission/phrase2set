\set ECHO all

--total code elements
select count(*) from 
	(select c.du, pqn, simple as name, kind
		from --random_du l, 
			clt c 
		where 
		--trust > 0 are valid
		trust = 0 
		-- and l.du = c.du
		--if it is a stackdump it's = 8
		and trust_original <> 8 
		--only want answers, questions are junky
		--and tid <> c.du 
		--variables aren't code elements
		and kind <> 'variable' 
		--make each distinct
		group by c.du, pqn, simple, kind
		--order by c.du, pos
	) as r;

--number of code elements -- excluding questions
select count(*) from 
	(select c.du, pqn, simple as name, kind
		from --random_du l, 
			clt c 
		where 
		--trust > 0 are valid
		trust = 0 
		-- and l.du = c.du
		--if it is a stackdump it's = 8
		and trust_original <> 8 
		--only want answers, questions are junky
		and tid <> c.du 
		--variables aren't code elements
		and kind <> 'variable' 
		--make each distinct
		group by c.du, pqn, simple, kind
		--order by c.du, pos
	) as r;

--number of posts with at least one code element (\ie the ones we're using)
select count(*) from 
	(select c.du
		from --random_du l, 
			clt c 
		where 
		--trust > 0 are valid
		trust = 0 
		-- and l.du = c.du
		--if it is a stackdump it's = 8
		and trust_original <> 8 
		--only want answers, questions are junky
		--and tid <> c.du 
		--variables aren't code elements
		and kind <> 'variable' 
		--make each distinct
		group by c.du
		--order by c.du, pos
	) as r;

--(excluding questions) number of posts with at least one code element (\ie the ones we're using)
select count(*) from 
	(select c.du
		from --random_du l, 
			clt c 
		where 
		--trust > 0 are valid
		trust = 0 
		-- and l.du = c.du
		--if it is a stackdump it's = 8
		and trust_original <> 8 
		--only want answers, questions are junky
		and tid <> c.du 
		--variables aren't code elements
		and kind <> 'variable' 
		--make each distinct
		group by c.du
		--order by c.du, pos
	) as r;
