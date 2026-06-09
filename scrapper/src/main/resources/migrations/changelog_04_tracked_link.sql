create table tracked_link (
    chat_id bigint not null references chat(chat_id),
    link_id bigint not null references link(link_id),
    tag_id bigint not null references tag(tag_id),
    last_updated timestamp not null default now(),
    primary key (chat_id, link_id, tag_id)
);

create index idx_tracked_link_by_chat on tracked_link(chat_id);
create index idx_tracked_link_by_chat_and_tag on tracked_link(chat_id, tag_id);
