create table if not exists endpoint_hit
(
    id       serial constraint endpoint_hit_pk primary key,
    app_name varchar(128) not null,
    uri      varchar(128) not null,
    ip       varchar(40) not null,
    hit_time timestamp    not null
);

