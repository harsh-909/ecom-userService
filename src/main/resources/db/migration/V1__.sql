CREATE TABLE user
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    is_created   datetime NULL,
    last_updated datetime NULL,
    deleted      BIT(1) NOT NULL,
    first_name   VARCHAR(255) NULL,
    last_name    VARCHAR(255) NULL,
    email        VARCHAR(255) NULL,
    phone_number VARCHAR(255) NULL,
    password     VARCHAR(255) NULL,
    address      VARCHAR(255) NULL,
    is_verified  BIT(1) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE user_role_mapping
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE user_roles
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    is_created   datetime NULL,
    last_updated datetime NULL,
    deleted      BIT(1) NOT NULL,
    name         VARCHAR(255) NULL,
    CONSTRAINT pk_userroles PRIMARY KEY (id)
);

CREATE TABLE user_token
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    is_created   datetime NULL,
    last_updated datetime NULL,
    deleted      BIT(1) NOT NULL,
    token        VARCHAR(2048) NULL,
    user_id      BIGINT NULL,
    CONSTRAINT pk_usertoken PRIMARY KEY (id)
);

ALTER TABLE user_token
    ADD CONSTRAINT FK_USERTOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user_role_mapping
    ADD CONSTRAINT fk_userolmap_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user_role_mapping
    ADD CONSTRAINT fk_userolmap_on_user_roles FOREIGN KEY (role_id) REFERENCES user_roles (id);