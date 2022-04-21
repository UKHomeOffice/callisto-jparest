CREATE TABLE dummyEntityA
(
    id           SERIAL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL
);

CREATE TABLE dummyEntityB
(
    id           SERIAL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL
);

CREATE TABLE dummyEntityC
(
    id           SERIAL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL,
    description  varchar(800) NULL,
    index        integer NOT NULL
);

CREATE TABLE dummyEntityD
(
    id           SERIAL PRIMARY KEY,
    description  varchar(800) NULL
);

CREATE TABLE dummyEntityA_dummyEntityB
(
    dummyEntityA  integer NOT NULL REFERENCES dummyEntityA (id),
    dummyEntityB   integer NOT NULL REFERENCES dummyEntityB (id)
);
