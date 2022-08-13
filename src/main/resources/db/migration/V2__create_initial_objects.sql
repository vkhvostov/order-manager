-- create schema order_manager;

create table orders
(
    id     serial PRIMARY KEY,
    status VARCHAR
);

create table order_to_position
(
    order_id    INT,
    position_id INT
);

create table positions
(
    id         serial PRIMARY KEY,
    article_id VARCHAR,
    amount     INT
);
