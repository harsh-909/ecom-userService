CREATE TABLE user
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    is_created   datetime NULL,
    last_updated datetime NULL,
    is_deleted   BIT(1) NOT NULL,
    first_name   VARCHAR(255) NULL,
    last_name    VARCHAR(255) NULL,
    email        VARCHAR(255) NULL,
    phone_number VARCHAR(255) NULL,
    address      VARCHAR(255) NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);