INSERT INTO hashtag values (10, '#users');
INSERT INTO hashtag values (20, '#commands');

insert into logger (id, host, origin, details, stacktrace)
values (1, 'host', 'origin', 'details', 'stacktrace');

insert into logger_hashtag (logger_id, hashtag_id)
values (1, 10);

insert into logger_hashtag (logger_id, hashtag_id)
values (1, 20);
