CREATE TABLE dummyEntityA
(
    id           SERIAL PRIMARY KEY
);

CREATE TABLE dummyEntityB
(
    id           SERIAL PRIMARY KEY
);

CREATE TABLE dummyEntityC
(
    id           SERIAL PRIMARY KEY,
    description  varchar(800) NULL
);

CREATE TABLE dummyEntityA_dummyEntityB
(
    dummyEntityA  integer NOT NULL REFERENCES dummyEntityA (id),
    dummyEntityB   integer NOT NULL REFERENCES dummyEntityB (id)
);
