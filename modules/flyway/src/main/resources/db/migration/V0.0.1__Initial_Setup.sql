-- Enable Extension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Setup Function
-- Update TimeStamp when
CREATE FUNCTION set_update_time() RETURNS trigger AS '
  begin
    new.updated_at := ''now'';
    return new;
  end;
' LANGUAGE 'plpgsql';

CREATE TABLE m_tenants (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" varchar(30) NOT NULL UNIQUE,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL default current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_tenants FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_roles (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" varchar(30) NOT NULL UNIQUE,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_roles FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_users (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "role_id" uuid REFERENCES m_roles(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "login_id" varchar(50) NOT NULL UNIQUE,
    "name" varchar(20) NOT NULL,
    "password" varchar(100) NOT NULL,
    "e-mail" varchar(100) NOT NULL UNIQUE,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_users FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_saifu_main_categories (
    "id" serial  PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" TEXT,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_saifu_main_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_saifu_sub_categories (
    "id" serial PRIMARY KEY,
    "saifu_main_category_id" integer REFERENCES m_saifu_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" TEXT,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_saifu_sub_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_saifu (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "saifu_sub_category_id" integer REFERENCES m_saifu_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" TEXT,
    "initial_balance" bigint NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_saifu FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_saifu_transfers (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "from_saifu_id" uuid REFERENCES m_saifu(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "to_saifu_id" uuid REFERENCES m_saifu(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_saifu_transfers FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_income_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_income_main_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_income_sub_categories (
    "id" serial PRIMARY KEY,
    "income_main_category_id" INTEGER REFERENCES m_income_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_income_sub_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_incomes (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_incomes FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_income_details (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "income_id" uuid REFERENCES t_incomes(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "income_sub_category_id" integer REFERENCES m_income_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_income_details FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_expense_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" TEXT,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_expense_main_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_expense_sub_categories (
    "id" serial PRIMARY KEY,
    "expense_main_category_id" INTEGER REFERENCES m_expense_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" TEXT,
    "tax_deduction_flag" boolean NOT NULL DEFAULT FALSE,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_expense_sub_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_expenses (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "salary_deduction_income_id" uuid REFERENCES t_incomes(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_expenses FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_expense_details (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "expense_id" uuid REFERENCES t_expenses(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_expense_details FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_investment_item_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_investment_item_main_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_investment_item_sub_categories (
    "id" serial PRIMARY KEY,
    "investment_item_main_category_id" integer REFERENCES m_investment_item_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_investment_item_sub_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_investment_items (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "investment_item_sub_category_id" integer REFERENCES m_investment_item_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" text,
    "purchase_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_investment_items FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_investments (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "salary_deduction_income_id" uuid REFERENCES t_incomes(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_investments FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_investment_details (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "investment_id" uuid REFERENCES t_investments(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "investment_item_id" uuid REFERENCES m_investment_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_investment_details FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_investment_item_histories (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "investment_detail_id" uuid REFERENCES t_investment_details(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "investment_item_id" uuid REFERENCES m_investment_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "transaction_amount" bigint NOT NULL,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_investment_item_histories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_debt_item_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_debt_item_main_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_debt_item_sub_categories (
    "id" serial PRIMARY KEY,
    "debt_item_main_category_id" integer REFERENCES m_debt_item_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_debt_item_sub_categories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE m_debt_items (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "debt_item_sub_category_id" integer REFERENCES m_debt_item_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(50) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON m_debt_items FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_debts (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "salary_deduction_income_id" uuid REFERENCES t_incomes(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_debts FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_debt_details (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "debt_id" uuid REFERENCES t_debts(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "debt_item_id" uuid REFERENCES m_debt_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_debt_details FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_debt_item_histories (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "debt_detail_id" uuid REFERENCES t_debt_details(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "debt_item_id" uuid REFERENCES m_debt_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "transaction_amount" bigint NOT NULL,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_debt_item_histories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();

CREATE TABLE t_saifu_histories (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "saifu_id" uuid REFERENCES m_saifu(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "income_id" uuid,
    "expense_id" uuid,
    "investment_id" uuid,
    "debt_id" uuid,
    "saifu_transfer_id" uuid,
    "transaction_amount" bigint NOT NULL,
    "transaction_date" timestamp with time zone NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "updated_at" timestamp with time zone
);
CREATE TRIGGER update_tri BEFORE UPDATE ON t_saifu_histories FOR EACH ROW
  EXECUTE PROCEDURE set_update_time();
