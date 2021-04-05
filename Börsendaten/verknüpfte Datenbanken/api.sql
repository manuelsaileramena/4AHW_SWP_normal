drop database if exists api;
create database api;
use api;
select * from ibmsplit where splitCoefficient > 1;
select * from ibm;
select * from tslasplit where splitCoefficient > 1;
select * from tslasplit where close >600;