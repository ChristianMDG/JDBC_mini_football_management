create type enum_position as enum('GK','DEF','MIDF','STR');
create type enum_continent as enum('EUROPA','AFRICA','AMERICA','ASIA');
create table if not exists Team(
    id int primary key ,
    name varchar(50),
    continent enum_continent
);
create table  if not exists Player (
    id int primary key,
    name varchar(50) ,
    age int,
    position enum_position,
    goal_nb int,
    id_team int references team(id) on delete cascade
);