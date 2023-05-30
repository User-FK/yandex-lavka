--liquibase formatted sql

--changeset fyodor:1
CREATE TABLE courier(
    id SERIAL PRIMARY KEY,
    courier_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
)

--changeset fyodor:2
CREATE TABLE region(
    pk_id SERIAL PRIMARY KEY,
    id INTEGER,
    courier_id INTEGER REFERENCES courier(id)
)

--changeset fyodor:3
CREATE TABLE work_time(
    id SERIAL PRIMARY KEY,
    courier_id INTEGER REFERENCES courier(id),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
)

--changeset fyodor:4
CREATE TABLE order_table(
    id SERIAL PRIMARY KEY,
    courier_id INTEGER,
    group_id INTEGER,
    group_pos INTEGER,
    weight REAL NOT NULL,
    region INTEGER NOT NULL,
    cost INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    assignment_date DATE,
    completed_time TIMESTAMP
)

--changeset fyodor:5
CREATE TABLE delivery_hours(
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES order_table(id),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
)