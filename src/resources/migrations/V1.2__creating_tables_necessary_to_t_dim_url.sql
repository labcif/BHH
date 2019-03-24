create table if not exists t_ext_urls
(
  id              INTEGER
    primary key,
  url             LONGVARCHAR,
  title           LONGVARCHAR,
  visit_count     INTEGER default 0 not null,
  typed_count     INTEGER default 0 not null,
  last_visit_time INTEGER not null,
  hidden          INTEGER default 0 not null
);
create index IF NOT EXISTS urls_url_index
  on t_ext_urls (url);
create table if not exists t_ext_visits
(
  id                              INTEGER
    primary key,
  url                             INTEGER not null,
  visit_time                      INTEGER not null,
  from_visit                      INTEGER,
  transition                      INTEGER default 0 not null,
  segment_id                      INTEGER,
  visit_duration                  INTEGER default 0 not null,
  incremented_omnibox_typed_score BOOLEAN default FALSE not null
);
create index  if not exists  visits_from_index
  on t_ext_visits (from_visit);
create index  if not exists  visits_time_index
  on t_ext_visits (visit_time);
create index if not exists  visits_url_index
  on t_ext_visits (url);
create table if not exists t_clean_url
(
  url_natural_key INTEGER
    constraint t_clean_url_pk
      primary key autoincrement,
  url_full VARCHAR2(255),
  url_domain VARCHAR2(255),
  url_path VARCHAR2(255),
  url_title varchar(255),
  url_visit_count INTEGER(30),
  url_typed_count INTEGER(30),
  url_last_visit_time DATE,
  url_hidden INTEGER(5),
  url_visit_full_date DATE,
  url_visit_date DATE,
  url_visit_time DATE,
  url_from_visit NUMBER(5)
);


