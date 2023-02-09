CREATE TABLE profiles (
    id varchar(36) PRIMARY KEY,
    tenant_id varchar(36) NOT NULL,
    preferences varchar(80) NULL,
    bio varchar(800) NULL,
    phone_number varchar(20) NULL,
    dob TIMESTAMP WITH TIME ZONE NULL,
    first_release TIMESTAMP WITH TIME ZONE NULL,
    props json NULL
);