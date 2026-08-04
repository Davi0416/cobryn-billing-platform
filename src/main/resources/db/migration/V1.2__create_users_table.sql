CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255)             NOT NULL,
    email      VARCHAR(255)             NOT NULL UNIQUE,
    cpf        VARCHAR(11)              NOT NULL UNIQUE,
    phone      VARCHAR(15),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
