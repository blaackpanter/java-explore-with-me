create table if not exists users
(
    id   integer generated by default as identity
        constraint users_pk
            primary key,
    email varchar(255) not null
        constraint users_pk2
            unique,
    name  varchar(255) not null
);

create table if not exists category
(
    id   integer generated by default as identity
        constraint category_pk
            primary key,
    name varchar(50) not null
        constraint category_pk2
            unique
);

create table if not exists compilation
(
    id     integer generated by default as identity
        constraint compilation_pk
            primary key,
    pinned boolean default false not null,
    title  varchar(128)          not null
        constraint compilation_pk2
            unique
);

create table if not exists event
(
    id                 integer generated by default as identity
        constraint event_pk
            primary key,
    annotation         varchar(2000)        not null,
    description        varchar(7000)        not null,
    category_id        integer              not null
        constraint event_category_id_fk
            references category
            on update cascade on delete restrict,
    created            timestamp            not null,
    event_date         timestamp            not null,
    initiator_id       integer              not null
        constraint event_users_id_fk
            references users
            on update cascade on delete cascade,
    paid               boolean              not null,
    participant_limit  integer default 0    not null,
    published          timestamp,
    request_moderation boolean default true not null,
    state              varchar(15)          not null,
    title              varchar(255)         not null,
    lat                double precision     not null,
    lon                double precision     not null
);

create table if not exists request
(
    id           integer generated by default as identity
        constraint request_pk
            primary key,
    event_id     integer     not null
        constraint request_event_id_fk
            references event
            on update cascade on delete cascade,
    created      timestamp   not null,
    requester_id integer     not null
        constraint request_users_id_fk
            references users
            on update cascade on delete cascade,
    status       varchar(32) not null
);

create table if not exists event_compilation_connection
(
    id             integer generated by default as identity
        constraint event_compilation_connection_pk
            primary key,
    event_id       integer not null
        constraint event_compilation_connection_event_id_fk
            references event
            on update cascade on delete cascade,
    compilation_id integer not null
        constraint event_compilation_connection_compilation_id_fk
            references compilation
            on update cascade on delete cascade
);

create table if not exists comment
(
    id        integer generated by default as identity
        constraint comment_pk
            primary key,
    content      varchar(1000) not null,
    event_id  integer       not null
        constraint comment_event_id_fk
            references event
            on update cascade on delete cascade,
    author_id integer       not null
        constraint comment_users_id_fk
            references users
            on update cascade on delete cascade,
    created   timestamp     not null
);
