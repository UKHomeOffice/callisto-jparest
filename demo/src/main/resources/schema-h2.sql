CREATE TABLE profiles
(
    profile_id      SERIAL PRIMARY KEY,
    tenant_id       varchar(36) NOT NULL,
    preferences     varchar(80) NULL,
    bio             varchar(800) NULL,
    phone_number    varchar(20) NULL,
    dob             date NULL,
    first_release   date NULL      
);

CREATE TABLE artists
(
    artist_id           SERIAL PRIMARY KEY,
    profile_id          integer NOT NULL REFERENCES profiles (profile_id),
    tenant_id           varchar(36) NOT NULL,
    performance_name    varchar(80) NULL
);

CREATE TABLE concerts
(
    concert_id          SERIAL PRIMARY KEY,
    tenant_id           varchar(36) NOT NULL,
    concert_name        varchar(80) NULL
);

CREATE TABLE concert_artists
(
    concert_id  integer NOT NULL REFERENCES concerts (concert_id),
    artist_id   integer NOT NULL REFERENCES artists (artist_id)
);

CREATE TABLE records
(
    record_id       SERIAL PRIMARY KEY,
    tenant_id       varchar(36) NOT NULL,
    artist_id       integer NOT NULL REFERENCES artists (artist_id),
    record_name     varchar(800) NULL
);
