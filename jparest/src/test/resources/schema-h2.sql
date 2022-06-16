CREATE TABLE dummy_EntityA
(
    id           varchar(36) NOT NULL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL,
    profile_id   integer,
    index        integer
);

CREATE TABLE dummy_EntityB
(
    id           varchar(36) NOT NULL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL
);

CREATE TABLE dummy_EntityC
(
    id           varchar(36) NOT NULL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL,
    description  varchar(800) NULL,
    index        integer NOT NULL,
    profile_id   integer
);

CREATE TABLE dummy_EntityD
(
    id           varchar(36) NOT NULL PRIMARY KEY,
    description  varchar(800) NULL
);

CREATE TABLE dummy_EntityF
(
    id                  varchar(36) PRIMARY KEY,
    tenant_id           varchar(36) NOT NULL,
    dummy_entityC_id    varchar(36) NOT NULL REFERENCES dummy_EntityC (id)
);

CREATE TABLE dummy_EntityA_dummy_EntityB
(
    dummy_EntityA  varchar(36) NOT NULL REFERENCES dummy_EntityA (id),
    dummy_EntityB   varchar(36) NOT NULL REFERENCES dummy_EntityB (id)
);
