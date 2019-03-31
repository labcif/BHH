-- CHROME --
create table if not exists t_clean_blocked_websites
(
	id          INTEGER not null
		constraint t_ext_blocked_websites_pk
		primary key autoincrement,
    domain      LONGVARCHAR not null
);