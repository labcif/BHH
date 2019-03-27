-- CHROME --
CREATE TABLE if not exists t_ext_chrome_downloads
(
  id                   INTEGER PRIMARY KEY,
  guid                 VARCHAR NOT NULL,
  current_path         LONGVARCHAR NOT NULL,
  target_path          LONGVARCHAR NOT NULL,
  start_time           INTEGER NOT NULL,
  received_bytes       INTEGER NOT NULL,
  total_bytes          INTEGER NOT NULL,
  state                INTEGER NOT NULL,
  danger_type          INTEGER NOT NULL,
  interrupt_reason     INTEGER NOT NULL,
  hash                 BLOB NOT NULL,
  end_time             INTEGER NOT NULL,
  opened               INTEGER NOT NULL,
  referrer             VARCHAR NOT NULL,
  site_url             VARCHAR NOT NULL,
  tab_url              VARCHAR NOT NULL,
  tab_referrer_url     VARCHAR NOT NULL,
  http_method          VARCHAR NOT NULL,
  by_ext_id            VARCHAR NOT NULL,
  by_ext_name          VARCHAR NOT NULL,
  etag                 VARCHAR NOT NULL,
  last_modified        VARCHAR NOT NULL,
  mime_type            VARCHAR(255) NOT NULL,
  original_mime_type   VARCHAR(255) NOT NULL,
  last_access_time     INTEGER NOT NULL DEFAULT 0,
  transient            INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE if not exists t_ext_chrome_downloads_slices
(
  download_id          INTEGER NOT NULL,
  offset               INTEGER NOT NULL,
  received_bytes       INTEGER NOT NULL,
  finished             INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (download_id, offset)
);

CREATE TABLE if not exists t_ext_chrome_downloads_url_chains
(
  id                   INTEGER NOT NULL,
  chain_index          INTEGER NOT NULL,
  url                  LONGVARCHAR NOT NULL,
  PRIMARY KEY (id, chain_index)
);

CREATE TABLE if not exists t_ext_chrome_keyword_search_terms
(
  keyword_id           INTEGER NOT NULL,
  url_id               INTEGER NOT NULL,
  lower_term           LONGVARCHAR NOT NULL,
  term                 LONGVARCHAR NOT NULL
);

CREATE TABLE if not exists t_ext_chrome_meta
(
  key LONGVARCHAR      NOT NULL UNIQUE PRIMARY KEY,
  value LONGVARCHAR
);

CREATE TABLE if not exists t_ext_chrome_segment_usage
(
  id                   INTEGER PRIMARY KEY,
  segment_id           INTEGER NOT NULL,
  time_slot            INTEGER NOT NULL,
  visit_count          INTEGER DEFAULT 0 NOT NULL
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
  storage_key          INTEGER PRIMARY KEY NOT NULL,
  value                BLOB
);

CREATE TABLE if not exists t_ext_chrome_visit_source
(
  id                   INTEGER PRIMARY KEY,
  source               INTEGER NOT NULL
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
  interrupt_reason      INTEGER
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
  date                  INTEGER
);

create table if not exists t_clean_words
(
  id                    INTEGER
    constraint t_clean_words_pk
      primary key autoincrement,
  word                  VARCHAR2(255),
  source_full           VARCHAR2(255)
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


