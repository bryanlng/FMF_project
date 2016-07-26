# This is to create the database table for FMF Server

DROP DATABASE IF EXISTS fmf_server;
CREATE DATABASE IF NOT EXISTS fmf_server;

USE fmf_server;

CREATE TABLE non_location_fields( 
PhoneNumber VARCHAR(255), 
DateInMilliseconds BIGINT(100),
ChargingMode VARCHAR(255), 
MobileData VARCHAR(255) ,
GPS VARCHAR(255) ,
MobileNetwork VARCHAR(255),
Wifi VARCHAR(255) 
);

CREATE TABLE location_fields( 
PhoneNumber VARCHAR(255), 
DateTimeReceived VARCHAR(255),
DateInMilliseconds BIGINT(100),
BestLocation VARCHAR(255),
Location_1 VARCHAR(255),
Location_2 VARCHAR(255),
BatteryLevel INT(3) 
);