create table if not exists t_clean_special_websites
(
	special_websites_id           INTEGER not null constraint t_clean_special_websites_pk primary key autoincrement,
	special_websites_domain       VARCHAR(255) NOT NULL
);


CREATE TABLE if not exists t_clean_downloads
(
  downloads_id                    INTEGER constraint t_clean_downloads_pk primary key autoincrement,
  downloads_natural_key           INTEGER,
  downloads_full_url              VARCHAR(255),
  downloads_domain                VARCHAR(255),
  downloads_mime_type             VARCHAR(255),
  downloads_target_path           VARCHAR(255),
  downloads_beginning_full_date   DATE,
  downloads_beginning_date        DATE,
  downloads_beginning_time        DATE,
  downloads_ending_full_date      DATE,
  downloads_ending_date           DATE,
  downloads_ending_time           DATE,
  downloads_received_bytes        INTEGER,
  downloads_total_bytes           INTEGER,
  downloads_user_origin           VARCHAR(255) NOT NULL,
  downloads_browser_origin        VARCHAR(255) NOT NULL,
  downloads_profile_name          VARCHAR(255),
  downloads_filename_location     VARCHAR(255)  NOT NULL,
  downloads_operating_system     VARCHAR(255)  NOT NULL
);

create table if not exists t_clean_logins
(
  logins_id                   INTEGER constraint t_clean_logins_pk primary key autoincrement,
  logins_email                VARCHAR(255),
  logins_source_full          VARCHAR(255),
  logins_domain               VARCHAR(255),
  logins_username_value       VARCHAR(255),
  logins_available_password   INTEGER,
  logins_date                 INTEGER,
  logins_user_origin          VARCHAR(255) NOT NULL,
  logins_browser_origin       VARCHAR(255) NOT NULL,
  logins_table_origin         VARCHAR(255) NOT NULL,
  logins_profile_name         VARCHAR(255),
  logins_filename_location    VARCHAR(255)  NOT NULL,
  logins_operating_system    VARCHAR(255)  NOT NULL
);

create table if not exists t_clean_search_in_engines
(
  search_in_engines_id                INTEGER constraint t_clean_search_in_engines_pk primary key autoincrement,
  search_in_engines_words             VARCHAR(255),
  search_in_engines_source_full       VARCHAR(255),
  search_in_engines_user_origin       VARCHAR(255) NOT NULL,
  search_in_engines_browser_origin    VARCHAR(255) NOT NULL,
  search_in_engines_domain            VARCHAR(255),
  search_profile_name                 VARCHAR(255),
  search_filename_location            VARCHAR(255)  NOT NULL,
  search_visit_full_date             DATE,
  search_visit_date                   DATE,
  search_visit_time                  DATE,
  search_operating_system            VARCHAR(255)  NOT NULL
);
create table if not exists t_clean_url
(
  url_id                        INTEGER constraint t_clean_url_pk primary key autoincrement,
  url_natural_key               INTEGER,
  url_full                      VARCHAR(255),
  url_domain                    VARCHAR(255),
  url_path                      VARCHAR(255),
  url_title                     VARCHAR(255),
  url_typed                     INTEGER,
  url_hidden                    INTEGER,
  url_visit_full_date_start     DATE,
  url_visit_date_start          DATE,
  url_visit_time_start          DATE,
  url_user_origin               VARCHAR(255) NOT NULL,
  url_browser_origin            VARCHAR(255) NOT NULL,
  url_visit_duration            DATE,
  url_visit_full_date_end       DATE,
  url_visit_date_end            DATE,
  url_visit_time_end            DATE,
  url_profile_name              VARCHAR(255),
  url_filename_location         VARCHAR(255)  NOT NULL,
  url_operating_system         VARCHAR(255)  NOT NULL
);
