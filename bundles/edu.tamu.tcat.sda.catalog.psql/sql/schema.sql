CREATE TABLE sda.people (
   id bigserial NOT NULL,
   historical_figure json,
   active boolean NOT NULL DEFAULT TRUE, 
   
   CONSTRAINT people_pkey PRIMARY KEY (id)
);


CREATE TABLE sda.works (
   id bigserial NOT NULL,
   work json,
   active boolean NOT NULL DEFAULT TRUE,
   
   CONSTRAINT works_pkey PRIMARY KEY (id)
);