CREATE TABLE  m_tenants (
    "id" uuid PRIMARY KEY,
    "name" varchar(10) NOT NULL UNIQUE,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_roles (
    "id" uuid PRIMARY KEY,
    "name" varchar(10) NOT NULL UNIQUE,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_users (
    "id" uuid PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "role_id" uuid REFERENCES m_roles(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "login_id" varchar(50) NOT NULL UNIQUE,
    "name" varchar(20) NOT NULL,
    "password" varchar(100) NOT NULL,
    "e-mail" varchar(100) NOT NULL,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_saifu_main_categories (
    "id" serial  PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(20) NOT NULL,
    "explain" TEXT,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_saifu_sub_categories (
    "id" serial PRIMARY KEY,
    "saifu_main_category_id" integer REFERENCES m_saifu_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" TEXT,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_saifu (
    "id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "saifu_sub_category_id" integer REFERENCES m_saifu_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(20) NOT NULL,
    "explain" TEXT,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_saifu_transfers (
    "transaction_id" uuid PRIMARY KEY,
    "from_saifu_id" uuid REFERENCES m_saifu(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "to_saifu_id" uuid REFERENCES m_saifu(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_income_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(20) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_income_sub_categories (
    "id" serial PRIMARY KEY,
    "income_main_category_id" INTEGER REFERENCES m_income_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_incomes (
    "transaction_id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_income_details (
    "id" uuid PRIMARY KEY,
    "income_transaction_id" uuid REFERENCES t_incomes(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "income_sub_category_id" integer REFERENCES m_income_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_expense_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(20) NOT NULL,
    "explain" TEXT,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_expense_sub_categories (
    "id" serial PRIMARY KEY,
    "expense_main_category_id" INTEGER REFERENCES m_expense_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" TEXT,
    "tax_deduction_flag" boolean NOT NULL DEFAULT FALSE,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_expenses (
    "transaction_id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "salary_deduction_income_transaction_id" uuid REFERENCES t_incomes(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_expense_details (
    "id" uuid PRIMARY KEY,
    "expense_transaction_id" uuid REFERENCES t_expenses(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_investment_item_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(20) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_investment_item_sub_categories (
    "id" serial PRIMARY KEY,
    "investment_item_main_category_id" integer REFERENCES m_investment_item_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_investment_items (
    "id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "investment_item_sub_category_id" serial REFERENCES m_investment_item_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_investments (
    "transaction_id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "salary_deduction_income_transaction_id" uuid REFERENCES t_incomes(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_investment_details (
    "id" uuid PRIMARY KEY,
    "investment_transaction_id" uuid REFERENCES t_investments(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "investment_item_id" uuid REFERENCES m_investment_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_investment_item_histories (
    "id" uuid PRIMARY KEY,
    "investment_detail_id" uuid REFERENCES t_investment_details(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "investment_item_id" uuid REFERENCES m_investment_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "income" bigint NOT NULL CHECK (income >= 0),
    "outcome" bigint NOT NULL CHECK (outcome >= 0),
    "balance" bigint NOT NULL CHECK (balance >= 0),
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_debt_item_main_categories (
    "id" serial PRIMARY KEY,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(20) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_debt_item_sub_categories (
    "id" serial PRIMARY KEY,
    "debt_item_main_category_id" integer REFERENCES m_debt_item_main_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "tenant_id" uuid REFERENCES m_tenants(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE m_debt_items (
    "id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "debt_item_sub_category_id" serial REFERENCES m_debt_item_sub_categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "name" varchar(30) NOT NULL,
    "explain" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_debts (
    "transaction_id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES m_users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "salary_deduction_income_transaction_id" uuid REFERENCES t_incomes(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "total" bigint NOT NULL CHECK (total >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_debt_details (
    "id" uuid PRIMARY KEY,
    "debt_transaction_id" uuid REFERENCES t_debts(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "debt_item_id" uuid REFERENCES m_debt_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "amount" bigint NOT NULL CHECK (amount >= 0),
    "comment" text,
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_debt_item_histories (
    "id" uuid PRIMARY KEY,
    "debt_detail_id" uuid REFERENCES t_debt_details(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "debt_item_id" uuid REFERENCES m_debt_items(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "income" bigint NOT NULL CHECK (income >= 0),
    "outcome" bigint NOT NULL CHECK (outcome >= 0),
    "balance" bigint NOT NULL CHECK (balance >= 0),
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);

CREATE TABLE t_saifu_histories (
    "id" uuid PRIMARY KEY,
    "saifu_id" uuid REFERENCES m_saifu(id) ON DELETE SET NULL ON UPDATE CASCADE,
    "income_transaction_id" uuid REFERENCES t_incomes(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "expense_transaction_id" uuid REFERENCES t_expenses(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "investment_transaction_id" uuid REFERENCES t_investments(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "debt_transaction_id" uuid REFERENCES t_debts(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "saifu_transfer_transaction_id" uuid REFERENCES t_saifu_transfers(transaction_id) ON DELETE SET NULL ON UPDATE CASCADE,
    "income" bigint NOT NULL CHECK (income >= 0),
    "outcome" bigint NOT NULL CHECK (outcome >= 0),
    "balance" bigint NOT NULL CHECk (balance >= 0),
    "delete_flag" boolean NOT NULL DEFAULT FALSE,
    "created" timestamp with time zone NOT NULL,
    "updated" timestamp with time zone
);
