/*==============================================================*/
/* Database name:  PHYSICALDATAMODEL_2                          */
/* DBMS name:      PostgreSQL 7                                 */
/* Created on:     12.04.2004 13:48:07                          */
/*==============================================================*/


drop table T_CATEGORY;

drop table T_CONFIGURATION;

drop table T_PROPERTY;

drop table T_VARIABLE;

/*==============================================================*/
/* Table: T_CATEGORY                                            */
/*==============================================================*/
create table T_CATEGORY (
OID                  SERIAL not null,
NAME                 VARCHAR(255)         null,
CONFIGURATION_OID    INT4                 null,
constraint PK_T_CATEGORY primary key (OID)
);

/*==============================================================*/
/* Table: T_CONFIGURATION                                       */
/*==============================================================*/
create table T_CONFIGURATION (
OID                  SERIAL not null,
NAME                 VARCHAR(255)         not null,
constraint PK_T_CONFIGURATION primary key (OID),
constraint AK_KEY_2_T_CONFIG unique (NAME)
);

/*==============================================================*/
/* Table: T_PROPERTY                                            */
/*==============================================================*/
create table T_PROPERTY (
OID                  SERIAL not null,
NAME                 VARCHAR(255)         null,
VALUE                VARCHAR(255)         null,
CATEGORY_OID         INT4                 not null,
constraint PK_T_PROPERTY primary key (OID)
);

/*==============================================================*/
/* Table: T_VARIABLE                                            */
/*==============================================================*/
create table T_VARIABLE (
OID                  SERIAL not null,
NAME                 VARCHAR(255)         null,
VALUE                VARCHAR(255)         null,
CONFIGURATION_OID    INT4                 null,
constraint PK_T_VARIABLE primary key (OID)
);

alter table T_CATEGORY
   add constraint FK_T_CATEGO_CAT_CON_R_T_CONFIG foreign key (CONFIGURATION_OID)
      references T_CONFIGURATION (OID)
      on delete restrict on update restrict;

alter table T_PROPERTY
   add constraint FK_T_PROPER_PRO_CAT_R_T_CATEGO foreign key (CATEGORY_OID)
      references T_CATEGORY (OID)
      on delete restrict on update restrict;

alter table T_VARIABLE
   add constraint FK_T_VARIAB_VAR_CON_R_T_CONFIG foreign key (CONFIGURATION_OID)
      references T_CONFIGURATION (OID)
      on delete restrict on update restrict;

