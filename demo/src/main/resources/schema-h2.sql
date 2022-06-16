CREATE TABLE profiles
(
    id              varchar(36) PRIMARY KEY,
    tenant_id       varchar(36) NOT NULL,
    preferences     varchar(80) NULL,
    bio             varchar(800) NULL,
    phone_number    varchar(20) NULL,
    dob             date NULL,
    first_release   date NULL      
);

CREATE TABLE artists
(
    id                  varchar(36) PRIMARY KEY,
    tenant_id           varchar(36) NOT NULL,
    performance_name    varchar(80) NULL,
    profile_id          varchar(36) NOT NULL REFERENCES profiles (id)
);

CREATE TABLE concerts
(
    id                  varchar(36) PRIMARY KEY,
    tenant_id           varchar(36) NOT NULL,
    concert_name        varchar(80) NULL
);

CREATE TABLE concert_artists
(
    concert_id  varchar(36) NOT NULL REFERENCES concerts (id),
    artist_id   varchar(36) NOT NULL REFERENCES artists (id)
);

CREATE TABLE records
(
    id              varchar(36) PRIMARY KEY,
    tenant_id       varchar(36) NOT NULL,
    artist_id       varchar(36) NOT NULL REFERENCES artists (id),
    record_name     varchar(800) NULL
);
