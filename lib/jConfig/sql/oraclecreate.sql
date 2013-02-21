/*==============================================================*/
/* Database name:  PHYSICALDATAMODEL_2                          */
/* DBMS name:      ORACLE Version 8i2 (8.1.6)                   */
/* Created on:     12.04.2004 13:47:18                          */
/*==============================================================*/


drop table T_CATEGORY cascade constraints
/


drop table T_CONFIGURATION cascade constraints
/


drop table T_PROPERTY cascade constraints
/


drop table T_VARIABLE cascade constraints
/


/*==============================================================*/
/* Table: T_CATEGORY                                            */
/*==============================================================*/


create table T_CATEGORY  (
   OID                  NUMBER(6)                        not null,
   NAME                 VARCHAR2(255),
   CONFIGURATION_OID    INTEGER,
   constraint PK_T_CATEGORY primary key (OID)
)
/


/*==============================================================*/
/* Table: T_CONFIGURATION                                       */
/*==============================================================*/


create table T_CONFIGURATION  (
   OID                  NUMBER(6)                        not null,
   NAME                 VARCHAR2(255)                    not null,
   constraint PK_T_CONFIGURATION primary key (OID),
   constraint AK_KEY_2_T_CONFIG unique (NAME)
)
/


/*==============================================================*/
/* Table: T_PROPERTY                                            */
/*==============================================================*/


create table T_PROPERTY  (
   OID                  NUMBER(6)                        not null,
   NAME                 VARCHAR2(255),
   VALUE                VARCHAR2(255),
   CATEGORY_OID         INTEGER                          not null,
   constraint PK_T_PROPERTY primary key (OID)
)
/


/*==============================================================*/
/* Table: T_VARIABLE                                            */
/*==============================================================*/


create table T_VARIABLE  (
   OID                  NUMBER(6)                        not null,
   NAME                 VARCHAR2(255),
   VALUE                VARCHAR2(255),
   CONFIGURATION_OID    INTEGER,
   constraint PK_T_VARIABLE primary key (OID)
)
/


alter table T_CATEGORY
   add constraint FK_T_CATEGO_CAT_CON_R_T_CONFIG foreign key (CONFIGURATION_OID)
      references T_CONFIGURATION (OID)
/


alter table T_PROPERTY
   add constraint FK_T_PROPER_PRO_CAT_R_T_CATEGO foreign key (CATEGORY_OID)
      references T_CATEGORY (OID)
/


alter table T_VARIABLE
   add constraint FK_T_VARIAB_VAR_CON_R_T_CONFIG foreign key (CONFIGURATION_OID)
      references T_CONFIGURATION (OID)
/


