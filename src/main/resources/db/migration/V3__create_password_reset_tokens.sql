create table password_reset_tokens (
  id varchar(255) primary key,
  token varchar(255) not null unique,
  user_id varchar(255) not null,
  expiry_date timestamp not null,
  used boolean not null default false
);
