-- CHROME --
create table if not exists t_ext_blocked_websites
(
	id          INTEGER not null
		constraint t_ext_blocked_websites_pk
			primary key autoincrement,
	domain      LONGVARCHAR not null
);

CREATE TABLE if not exists t_ext_chrome_login_data
(
  origin_url            VARCHAR,
  action_url            VARCHAR,
  username_element      VARCHAR,
  username_value        VARCHAR,
  password_element      VARCHAR,
  password_value        BLOB,
  submit_element        VARCHAR,
  signon_realm          VARCHAR,
  preferred             INTEGER,
  date_created          INTEGER,
  blacklisted_by_user   INTEGER,
  scheme                INTEGER,
  password_type         INTEGER,
  times_used            INTEGER,
  form_data             BLOB,
  date_synced           INTEGER,
  display_name          VARCHAR,
  icon_url              VARCHAR,
  federation_url        VARCHAR,
  skip_zero_click       INTEGER,
  generation_upload_status INTEGER,
  possible_username_pairs BLOB,
  id                    INTEGER
);