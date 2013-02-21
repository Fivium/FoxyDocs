--==============================================================*/
-- Database name:                            */
-- DBMS name:      INFORMIX                                 */
-- Created on:     12.04.2004 13:48:07                          */
--==============================================================*/


drop table T_CATEGORY;

drop table T_CONFIGURATION;

drop table T_PROPERTY;

drop table T_VARIABLE;

--==============================================================*/
-- Table: T_CATEGORY                                            */
--==============================================================*/
create table T_CATEGORY (
OID                  SERIAL not null,
NAME                 VARCHAR(255),
CONFIGURATION_OID    INTEGER
);
ALTER TABLE T_CATEGORY ADD constraint primary key (OID) constraint PK_T_CATEGORY;

--==============================================================*/
-- Table: T_CONFIGURATION                                       */
--==============================================================*/
create table T_CONFIGURATION (
OID                  SERIAL not null,
NAME                 VARCHAR(255)         not null);

ALTER TABLE T_CONFIGURATION ADD constraint primary key (OID) constraint PK_T_CONFIGURATION;
--ALTER TABLE T_CONFIGURATION ADD constraint unique (NAME) constraint AK_KEY_2_T_CONFIG;


--==============================================================*/
-- Table: T_PROPERTY                                            */
--==============================================================*/
create table T_PROPERTY (
OID                  SERIAL not null,
NAME                 VARCHAR(255)         ,
VALUE                VARCHAR(255)         ,
CATEGORY_OID         INTEGER              not null
);
ALTER TABLE T_PROPERTY ADD constraint primary key (OID) constraint PK_T_PROPERTY;

--==============================================================*/
-- Table: T_VARIABLE                                            */
--==============================================================*/
create table T_VARIABLE (
OID                  SERIAL not null,
NAME                 VARCHAR(255)         ,
VALUE                VARCHAR(255)         ,
CONFIGURATION_OID    INTEGER
);
ALTER TABLE T_VARIABLE ADD constraint primary key (OID) constraint PK_T_VARIABLE;


alter table T_CATEGORY
   add constraint (foreign key (CONFIGURATION_OID)
      references T_CONFIGURATION (OID)
      on delete cascade  constraint FK_T_CATEGO) ;

alter table T_PROPERTY
   add constraint  (foreign key (CATEGORY_OID)
      references T_CATEGORY (OID)
      on delete cascade constraint FK_T_PROPER) ;

alter table T_VARIABLE
   add constraint  (foreign key (CONFIGURATION_OID)
      references T_CONFIGURATION (OID)
       on delete cascade constraint FK_T_VARIAB);
