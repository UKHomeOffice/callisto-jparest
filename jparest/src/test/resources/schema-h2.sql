CREATE TABLE dummy_EntityA
(
    id           SERIAL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL
);

CREATE TABLE dummy_EntityB
(
    id           SERIAL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL
);

CREATE TABLE dummy_EntityC
(
    id           SERIAL PRIMARY KEY,
    tenant_id    varchar(36) NOT NULL,
    description  varchar(800) NULL,
    index        integer NOT NULL
);

CREATE TABLE dummy_EntityD
(
    id           SERIAL PRIMARY KEY,
    description  varchar(800) NULL
);

CREATE TABLE dummy_EntityA_dummy_EntityB
(
    dummy_EntityA  integer NOT NULL REFERENCES dummy_EntityA (id),
    dummy_EntityB   integer NOT NULL REFERENCES dummy_EntityB (id)
);
