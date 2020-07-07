DROP TABLE rates_marks IF EXISTS;
DROP TABLE rates IF EXISTS;
DROP SEQUENCE global_seq IF EXISTS;

create sequence global_seq;

CREATE TABLE rates
(
    id               BIGINT DEFAULT global_seq.nextval PRIMARY KEY,
    user_id          VARCHAR(255)            NOT NULL,
    message_id       VARCHAR(225)            NOT NULL,
    comment          VARCHAR(255),
    marking          BOOLEAN                 NOT NULL,
    date             DATE DEFAULT now()      NOT NULL,
    UNIQUE (user_id, date)
);

CREATE TABLE rates_marks
(
    id               BIGINT DEFAULT global_seq.nextval PRIMARY KEY,
    rate_id          INTEGER                    NOT NULL,
    mark             VARCHAR(225)               NOT NULL,
    FOREIGN KEY (rate_id) REFERENCES rates (id),
    UNIQUE (rate_id, mark)
)