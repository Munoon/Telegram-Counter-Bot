DROP TABLE rates_marks IF EXISTS;
DROP TABLE rates IF EXISTS;
DROP TABLE messages IF EXISTS;
DROP SEQUENCE global_seq IF EXISTS;

create sequence global_seq;

CREATE TABLE rates
(
    id               BIGINT DEFAULT global_seq.nextval PRIMARY KEY,
    user_id          VARCHAR(255)            NOT NULL,
    message_id       VARCHAR(225)            NOT NULL,
    comment          TEXT,
    marking          BOOLEAN                 NOT NULL,
    date             DATE DEFAULT now()      NOT NULL
);

CREATE TABLE rates_marks
(
    id               BIGINT DEFAULT global_seq.nextval PRIMARY KEY,
    rate_id          INTEGER                    NOT NULL,
    mark             VARCHAR(225)               NOT NULL,
    FOREIGN KEY (rate_id) REFERENCES rates (id),
    UNIQUE (rate_id, mark)
);

CREATE TABLE messages
(
    id               BIGINT DEFAULT global_seq.nextval PRIMARY KEY,
    user_id          TEXT                       NOT NULL,
    message          TEXT                       NOT NULL,
    date             DATE DEFAULT now()         NOT NULL
);