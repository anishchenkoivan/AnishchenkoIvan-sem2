create table authors (
    id bigserial primary key,
    first_name text,
    last_name text
);

create table books (
    id bigserial primary key,
    author_id bigint references authors(id),
    title text,
    rating integer,
    status smallint
);

create table tags (
    id bigserial primary key,
    name text
);

create table book_tag (
    book_id bigint references books(id),
    tag_id bigint references tags(id),
    primary key (tag_id, book_id)
);

create table outbox (
    id bigserial primary key,
    data text
);