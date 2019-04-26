alter table t_clean_words
	add url_domain VARCHAR(255);
alter table t_clean_url
	add url_visit_duration INTEGER;