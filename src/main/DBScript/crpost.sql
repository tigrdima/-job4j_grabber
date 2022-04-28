create table post (
    id serial primary key,
    title text,
    link text UNIQUE,
    description text,
    created timestamp
)