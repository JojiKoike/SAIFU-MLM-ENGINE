create table  m_tenants (
    "id" uuid primary key,
    "name" varchar(10),
    "explain" text,
    "delete_flag" boolean,
    "created" timestamp with time zone,
    "updated" timestamp with time zone
);

create table m_roles (
    "id" uuid primary key,
    "name" varchar(10),
    "explain" text,
    "delete_flag" boolean,
    "created" timestamp with time zone,
    "updated" timestamp with time zone
);

create table m_users (
    "id" uuid primary key,
    "tenant_id" uuid references m_tenants(id),
    "role_id" uuid references m_roles(id),
    "login_id" varchar(50),
    "password" varchar(100),
    "e-mail" varchar(50),
    "delete_flag" boolean,
    "created" timestamp with time zone,
    "updated" timestamp with time zone
);
