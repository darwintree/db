USE MYFILESYSTEM;

drop table my_file;

drop table my_folder;

create table my_folder(
	ID int,
    name varchar(200),
    parentDirID int,
    lastModifyTime long,
    access int,
    depth int,
    primary key (ID),
    foreign key(parentDirID) references my_folder(ID) on delete cascade);

create table my_file(
	ID int,
    name varchar(200),
    parentDirID int,
    lastModifyTime long,
    size long,
    occupied_space long,
    access int,
    depth int,
    primary key (ID),
    foreign key(parentDirID) references my_folder(ID) on delete cascade);
    


show tables;

select * from my_file;
select * from my_folder;

