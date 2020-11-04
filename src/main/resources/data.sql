create table hash (
  id int identity primary key auto_increment,
  description varchar(50) not null
);

INSERT INTO hash (description) values ('#users');
INSERT INTO hash (description) values ('#commands');

create table log (
  id int identity primary key auto_increment,
  created timestamp default current_timestamp,
  host varchar(100),
  origin varchar(100),
  details varchar,
  stacktrace varchar
);

create table loghash (
  id int identity primary key auto_increment,
  log_id int not null,
  hash_id int not null,
  foreign key (log_id) references log(id),
  foreign key (hash_id) references hash(id)
);
