create table tag (
    tag_id bigserial primary key,
    tag varchar(128) not null unique
);