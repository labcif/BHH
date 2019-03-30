-- CHROME --
CREATE TABLE if not exists t_ext_chrome_downloads
(
  id                   INTEGER PRIMARY KEY,
  guid                 VARCHAR,
  current_path         LONGVARCHAR,
  target_path          LONGVARCHAR,
  start_time           INTEGER,
  received_bytes       INTEGER,
  total_bytes          INTEGER,
  state                INTEGER,
  danger_type          INTEGER,
  interrupt_reason     INTEGER,
  hash                 BLOB,
  end_time             INTEGER,
  opened               INTEGER,
  referrer             VARCHAR,
  site_url             VARCHAR,
  tab_url              VARCHAR,
  tab_referrer_url     VARCHAR,
  http_method          VARCHAR,
  by_ext_id            VARCHAR,
  by_ext_name          VARCHAR,
  etag                 VARCHAR,
  last_modified        VARCHAR,
  mime_type            VARCHAR(255),
  original_mime_type   VARCHAR(255),
  last_access_time     INTEGER DEFAULT 0,
  transient            INTEGER DEFAULT 0
);

CREATE TABLE if not exists t_ext_chrome_downloads_slices
(
  download_id          INTEGER,
  offset               INTEGER,
  received_bytes       INTEGER,
  finished             INTEGER DEFAULT 0,
  PRIMARY KEY (download_id, offset)
);

CREATE TABLE if not exists t_ext_chrome_downloads_url_chains
(
  id                   INTEGER ,
  chain_index          INTEGER ,
  url                  LONGVARCHAR ,
  PRIMARY KEY (id, chain_index)
);

CREATE TABLE if not exists t_ext_chrome_keyword_search_terms
(
  keyword_id           INTEGER ,
  url_id               INTEGER ,
  lower_term           LONGVARCHAR ,
  term                 LONGVARCHAR
);

CREATE TABLE if not exists t_ext_chrome_meta
(
  key LONGVARCHAR       UNIQUE PRIMARY KEY,
  value LONGVARCHAR
);

CREATE TABLE if not exists t_ext_chrome_segment_usage
(
  id                   INTEGER PRIMARY KEY,
  segment_id           INTEGER ,
  time_slot            INTEGER ,
  visit_count          INTEGER DEFAULT 0
);

CREATE TABLE if not exists t_ext_chrome_segments
(
  id                   INTEGER PRIMARY KEY,
  name                 VARCHAR,
  url_id               INTEGER NON NULL
);

CREATE TABLE if not exists t_ext_chrome_sqlite_sequence
(
  name,
  seq
);

CREATE TABLE if not exists t_ext_chrome_typed_url_sync_metadata
(
  storage_key          INTEGER PRIMARY KEY ,
  value                BLOB
);

CREATE TABLE if not exists t_ext_chrome_visit_source
(
  id                   INTEGER PRIMARY KEY,
  source               INTEGER
);

CREATE TABLE if not exists t_clean_downloads
(
  id                   INTEGER
    constraint t_clean_downloads_pk
      primary key autoincrement,
  url_domain            VARCHAR2(255),
  url_full              VARCHAR2(255),
  type                  INTEGER,
  danger_type           INTEGER,
  tab_url               VARCHAR2(255),
  download_target_path  VARCHAR2(255),
  beginning_date        INTEGER,
  ending_date           INTEGER,
  received_bytes        INTEGER,
  total_bytes           INTEGER,
  interrupt_reason      INTEGER,
  url_user_origin varchar(255) NOT NULL,
  url_browser_origin varchar(255) NOT NULL
);

create table if not exists t_clean_emails
(
  id                    INTEGER
    constraint t_clean_emails_pk
      primary key autoincrement,
  email                 VARCHAR2(255),
  source_full           VARCHAR2(255),
  original_url          VARCHAR2(255),
  username_value        VARCHAR2(255),
  available_password    INTEGER,
  date                  INTEGER,
  url_user_origin varchar(255) NOT NULL,
  url_browser_origin varchar(255) NOT NULL
);

create table if not exists t_clean_words
(
  id                    INTEGER
    constraint t_clean_words_pk
      primary key autoincrement,
  word                  VARCHAR2(255),
  source_full           VARCHAR2(255),
  url_user_origin varchar(255) NOT NULL,
  url_browser_origin varchar(255) NOT NULL
);

CREATE INDEX if not exists keyword_search_terms_index1
  ON t_ext_chrome_keyword_search_terms (keyword_id, lower_term);

CREATE INDEX if not exists keyword_search_terms_index2
  ON t_ext_chrome_keyword_search_terms (url_id);

CREATE INDEX if not exists keyword_search_terms_index3
  ON t_ext_chrome_keyword_search_terms (term);

CREATE INDEX if not exists segment_usage_time_slot_segment_id
  ON t_ext_chrome_segment_usage(time_slot, segment_id);

CREATE INDEX if not exists segments_name
  ON t_ext_chrome_segments(name);

CREATE INDEX if not exists segments_url_id
  ON t_ext_chrome_segments(url_id);

CREATE INDEX if not exists segments_usage_seg_id
  ON t_ext_chrome_segment_usage(segment_id);

CREATE INDEX if not exists urls_url_index
  ON t_ext_chrome_urls (url);

CREATE INDEX if not exists visits_time_index
  ON t_ext_chrome_visits (visit_time);

CREATE INDEX if not exists visits_url_index
  ON t_ext_chrome_visits (url);


