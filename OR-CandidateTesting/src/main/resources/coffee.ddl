CREATE TABLE IF NOT EXISTS ORDERS
(
    ORDER_ID        VARCHAR(64)              NOT NULL PRIMARY KEY,
    TOKEN           VARCHAR(64)              NOT NULL,
    MACHINE_ID      VARCHAR(64)              NOT NULL,
    COFFEE          INTEGER                  NOT NULL,
    READY_DATE_TIME TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS MACHINES
(
    MACHINE_ID   VARCHAR NOT NULL PRIMARY KEY,
    FLOOR        VARCHAR NOT NULL,
    KITCHEN      VARCHAR NOT NULL,
    VELOCITY     INTEGER NOT NULL,
    AVAILABILITY TIMESTAMP WITH TIME ZONE
);

ALTER TABLE ORDERS
    ADD CONSTRAINT FK_ORDERS_MACHINE_ID FOREIGN KEY (MACHINE_ID) REFERENCES MACHINES (MACHINE_ID);

INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Lion', 'Floor 0', 'Kitchen Animals', 1);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Turtle', 'Floor 0', 'Kitchen Animals', 7);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Elephant', 'Floor 0', 'Kitchen Animals', 4);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Superman', 'Floor 1', 'Kitchen Hero', 1);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Hulk', 'Floor 1', 'Kitchen Hero', 6);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('DonQuixote', 'Floor 2', 'Kitchen Books', 5);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Holmes', 'Floor 2', 'Kitchen Books', 3);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Rock', 'Floor 2', 'Kitchen Music', 2);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Jazz', 'Floor 2', 'Kitchen Music', 7);
INSERT INTO MACHINES(MACHINE_ID, FLOOR, KITCHEN, VELOCITY)
VALUES ('Folk', 'Floor 2', 'Kitchen Music', 4);