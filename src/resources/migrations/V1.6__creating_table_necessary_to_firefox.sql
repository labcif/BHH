
CREATE TABLE if not exists t_ext_mozila_anno_attributes
(
  id                      INTEGER PRIMARY KEY,
  name                    VARCHAR(32) UNIQUE NOT NULL
);

CREATE TABLE if not exists t_ext_mozila_annos
(
  id                      INTEGER PRIMARY KEY,
  place_id                INTEGER NOT NULL,
  anno_attribute_id       INTEGER,
  mime_type               VARCHAR(32) DEFAULT NULL,
  content                 LONGVARCHAR,
  flags                   INTEGER DEFAULT 0,
  expiration              INTEGER DEFAULT 0,
  type                    INTEGER DEFAULT 0,
  dateAdded               INTEGER DEFAULT 0,
  lastModified            INTEGER DEFAULT 0
);

CREATE TABLE if not exists t_ext_mozila_bookmarks
(
  id                      INTEGER PRIMARY KEY,
  type                    INTEGER,
  fk                      INTEGER DEFAULT NULL,
  parent                  INTEGER,
  position                INTEGER,
  title                   LONGVARCHAR,
  keyword_id              INTEGER,
  folder_type             LONGVARCHAR,
  dateAdded               INTEGER,
  lastModified            INTEGER,
  guid                    LONGVARCHAR,
  syncStatus              INTEGER DEFAULT 0 NOT NULL,
  syncChangeCounter       INTEGER DEFAULT 1 NOT NULL
);

CREATE TABLE if not exists t_ext_mozila_bookmarks_deleted
(
  guid                    LONGVARCHAR PRIMARY KEY,
  dateRemoved             INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE if not exists t_ext_mozila_historyvisits
(
  id                      INTEGER PRIMARY KEY,
  from_visit              INTEGER,
  place_id                INTEGER,
  visit_date              INTEGER,
  visit_type              INTEGER,
  session                 INTEGER
);

CREATE TABLE if not exists t_ext_mozila_hosts
(
  id                      INTEGER PRIMARY KEY,
  host                    LONGVARCHAR NOT NULL UNIQUE,
  frecency                INTEGER,
  typed                   INTEGER NOT NULL DEFAULT 0,
  prefix                  LONGVARCHAR
);

CREATE TABLE if not exists t_ext_mozila_inputhistory
(
  place_id                INTEGER NOT NULL,
  input                   LONGVARCHAR NOT NULL,
  use_count               INTEGER,
  PRIMARY KEY (place_id, input)
);

CREATE TABLE if not exists t_ext_mozila_items_annos
(
  id                      INTEGER PRIMARY KEY,
  item_id                 INTEGER NOT NULL,
  anno_attribute_id       INTEGER,
  mime_type               VARCHAR(32) DEFAULT NULL,
  content                 LONGVARCHAR,
  flags                   INTEGER DEFAULT 0,
  expiration              INTEGER DEFAULT 0,
  type                    INTEGER DEFAULT 0,
  dateAdded               INTEGER DEFAULT 0,
  lastModified            INTEGER DEFAULT 0
);

CREATE TABLE if not exists t_ext_mozila_keywords
(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  keyword                LONGVARCHAR UNIQUE,
  place_id               INTEGER,
  post_data              LONGVARCHAR
);

CREATE TABLE if not exists t_ext_mozila_meta
(
  key                    LONGVARCHAR PRIMARY KEY,
  value                  LONGVARCHAR NOT NULL
) WITHOUT ROWID ;

CREATE TABLE if not exists t_ext_mozila_origins
(
  id                     INTEGER PRIMARY KEY,
  prefix                 LONGVARCHAR NOT NULL,
  host                   LONGVARCHAR NOT NULL,
  frecency               INTEGER NOT NULL,
  UNIQUE (prefix, host)
);

CREATE TABLE if not exists t_ext_mozila_places
(
  id                     INTEGER PRIMARY KEY,
  url                    LONGVARCHAR,
  title                  LONGVARCHAR,
  rev_host               LONGVARCHAR,
  visit_count            INTEGER DEFAULT 0,
  hidden                 INTEGER DEFAULT 0 NOT NULL,
  typed                  INTEGER DEFAULT 0 NOT NULL,
  favicon_id             INTEGER,
  frecency               INTEGER DEFAULT -1 NOT NULL,
  last_visit_date        INTEGER ,
  guid                   LONGVARCHAR,
  foreign_count          INTEGER DEFAULT 0 NOT NULL,
  url_hash               INTEGER DEFAULT 0 NOT NULL ,
  description            LONGVARCHAR,
  preview_image_url      LONGVARCHAR,
  origin_id              INTEGER
);


CREATE UNIQUE INDEX if not exists moz_annos_placeattributeindex
  ON t_ext_mozila_annos (place_id, anno_attribute_id);

CREATE INDEX if not exists moz_bookmarks_dateaddedindex
  ON t_ext_mozila_bookmarks (dateAdded);

CREATE UNIQUE INDEX if not exists moz_bookmarks_guid_uniqueindex
  ON t_ext_mozila_bookmarks (guid);

CREATE INDEX if not exists moz_bookmarks_itemindex
  ON t_ext_mozila_bookmarks (fk, type);

CREATE INDEX if not exists moz_bookmarks_itemlastmodifiedindex
  ON t_ext_mozila_bookmarks (fk, lastModified);

CREATE INDEX if not exists moz_bookmarks_parentindex
  ON t_ext_mozila_bookmarks (parent, position);

CREATE INDEX if not exists moz_historyvisits_dateindex
  ON t_ext_mozila_historyvisits (visit_date);

CREATE INDEX if not exists moz_historyvisits_fromindex
  ON t_ext_mozila_historyvisits (from_visit);

CREATE INDEX if not exists moz_historyvisits_placedateindex
  ON t_ext_mozila_historyvisits (place_id, visit_date);

CREATE UNIQUE INDEX if not exists moz_items_annos_itemattributeindex
  ON t_ext_mozila_items_annos (item_id, anno_attribute_id);

CREATE UNIQUE INDEX if not exists moz_keywords_placepostdata_uniqueindex
  ON t_ext_mozila_keywords (place_id, post_data);

CREATE INDEX if not exists moz_places_frecencyindex
  ON t_ext_mozila_places (frecency);

CREATE UNIQUE INDEX if not exists moz_places_guid_uniqueindex
  ON t_ext_mozila_places (guid);

CREATE INDEX if not exists moz_places_hostindex
  ON t_ext_mozila_places (rev_host);

CREATE INDEX if not exists moz_places_lastvisitdateindex
  ON t_ext_mozila_places (last_visit_date);

CREATE INDEX if not exists moz_places_originidindex
  ON t_ext_mozila_places (origin_id);

CREATE INDEX if not exists moz_places_url_hashindex
  ON t_ext_mozila_places (url_hash);

CREATE INDEX if not exists moz_places_visitcount
  ON t_ext_mozila_places (visit_count);
