drop database if exists api;
create database api;
use api;
select * from ibmsplit where splitCoefficient > 1;
select * from ibm;
select * from tslasplit where splitCoefficient > 1;
update TSLA set close = 4.7780000000000005 where datum = "2010-06-29";
select * from aapltrade;
select * from aaplbuyandhold;
select * from aapltrade3;
select * from tslatrade;
select * from ibmtrade;
select * from admtrade;

select depot from tsla order by datum desc limit 1;