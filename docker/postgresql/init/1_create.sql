CREATE ROLE saifu_mlm_engine_db_user LOGIN PASSWORD 'password';
ALTER ROLE saifu_mlm_engine_db_user SUPERUSER;
CREATE DATABASE saifu_mlm_engine_db;
GRANT ALL PRIVILEGES ON DATABASE saifu_mlm_engine_db TO saifu_mlm_engine_db_user;
