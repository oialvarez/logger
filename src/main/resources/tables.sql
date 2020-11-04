create table hashtag (
  id int identity primary key auto_increment,
  description varchar(50) not null
);

create table logger (
  id int identity primary key auto_increment,
  created timestamp default current_timestamp,
  host varchar(100),
  origin varchar(100),
  details varchar,
  stacktrace varchar
);

create table logger_hashtag (
  id int identity primary key auto_increment,
  logger_id int not null,
  hashtag_id int not null,
  foreign key (logger_id) references logger(id),
  foreign key (hashtag_id) references hashtag(id)
);
