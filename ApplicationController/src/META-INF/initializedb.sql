PRAGMA auto_vacuum = FULL;
CREATE TABLE PUSH_AND_DEL (PAD_ID NUMBER(6) PRIMARY KEY, FIRST_NAME VARCHAR2(20), LAST_NAME VARCHAR2(25) NOT NULL);
INSERT INTO PUSH_AND_DEL (PAD_ID, FIRST_NAME, LAST_NAME) VALUES (100,'Natalie','Cole');
INSERT INTO PUSH_AND_DEL (PAD_ID, FIRST_NAME, LAST_NAME) VALUES (101,'Dean','Jones');
INSERT INTO PUSH_AND_DEL (PAD_ID, FIRST_NAME, LAST_NAME) VALUES (102,'Billy','Joe Royal');
INSERT INTO PUSH_AND_DEL (PAD_ID, FIRST_NAME, LAST_NAME) VALUES (103,'David','Bowie');
