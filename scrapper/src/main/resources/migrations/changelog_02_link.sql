create table link (
    link_id bigserial primary key,
    uri varchar(256) not null unique
);