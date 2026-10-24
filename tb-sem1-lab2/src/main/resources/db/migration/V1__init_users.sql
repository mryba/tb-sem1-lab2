create table users (
    id bigint generated always as identity primary key,
    email varchar(256) not null,
    password_hash varchar(255) not null,
    failed_attempts integer not null default 0,
    locked_until timestamptz,
    created_at timestamptz not null default now()
);

create unique index uq_users_email on users (email);
