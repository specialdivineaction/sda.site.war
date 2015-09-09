CREATE TABLE people (
   id varchar(255) NOT NULL,
   historical_figure json,
   active boolean NOT NULL DEFAULT true,
   state varchar(32) NOT NULL DEFAULT 'ACTIVE'::varchar,
   created timestamp without time zone NOT NULL DEFAULT now(),
   modified timestamp without time zone,
   CONSTRAINT people_pkey PRIMARY KEY (id)
);

CREATE TABLE works (
   id varchar(255) NOT NULL,
   work json,
   active boolean NOT NULL DEFAULT true,
   state varchar(32) NOT NULL DEFAULT 'ACTIVE'::varchar,
   created timestamp without time zone NOT NULL DEFAULT now(),
   modified timestamp without time zone,
   CONSTRAINT works_pkey PRIMARY KEY (id)
);

CREATE TABLE relationships (
   id varchar(255) NOT NULL,
   relationship json,
   active boolean NOT NULL DEFAULT true,
   state varchar(32) NOT NULL DEFAULT 'ACTIVE'::varchar,
   created timestamp without time zone NOT NULL DEFAULT now(),
   modified timestamp without time zone,
   CONSTRAINT relationships_pkey PRIMARY KEY (id)
);

CREATE TABLE copy_references (
   ref_id varchar(255) NOT NULL,
   reference json,
   active boolean NOT NULL DEFAULT true,
   state varchar(32) NOT NULL DEFAULT 'ACTIVE'::varchar,
   created timestamp without time zone NOT NULL DEFAULT now(),
   modified timestamp without time zone,
   CONSTRAINT copy_references_pkey PRIMARY KEY (ref_id)
);

CREATE TABLE articles (
   article_id varchar(255) NOT NULL,
   article json,
   active boolean NOT NULL DEFAULT true,
   state varchar(32) NOT NULL DEFAULT 'ACTIVE'::varchar,
   created timestamp without time zone NOT NULL DEFAULT now(),
   modified timestamp without time zone,
   CONSTRAINT articles_pkey PRIMARY KEY (article_id)
);

CREATE TABLE notes (
   note_id varchar(255) NOT NULL,
   note json,
   active boolean NOT NULL DEFAULT true,
   state varchar(32) NOT NULL DEFAULT 'ACTIVE'::varchar,
   created timestamp without time zone NOT NULL DEFAULT now(),
   modified timestamp without time zone,
   CONSTRAINT notes_pkey PRIMARY KEY (note_id)
);

CREATE TABLE id_table (
   context varchar(255) NOT NULL,
   next_int bigint,
   CONSTRAINT id_table_pkey PRIMARY KEY (context)
);