-- Esquema inicial. Los identificadores usan SEQUENCE (increment 50) para que
-- Hibernate pueda agrupar inserts en lote (JDBC batching no funciona con IDENTITY).

create sequence driver_seq       start with 1 increment by 50;
create sequence taxi_seq         start with 1 increment by 50;
create sequence daily_record_seq start with 1 increment by 50;

create table drivers (
    id              bigint       primary key,
    full_name       varchar(120) not null,
    document_number varchar(20)  not null,
    phone           varchar(20),
    active          boolean      not null default true,
    constraint uk_drivers_document unique (document_number)
);

create table taxis (
    id         bigint       primary key,
    plate      varchar(6)   not null,
    model      varchar(80)  not null,
    owner_name varchar(120) not null,
    active     boolean      not null default true,
    driver_id  bigint       references drivers (id),
    constraint uk_taxis_plate unique (plate),
    constraint ck_taxis_plate_format check (plate ~ '^[A-Z]{3}[0-9]{3}$')
);

create index ix_taxis_driver on taxis (driver_id);

create table daily_records (
    id                  bigint        primary key,
    version             bigint        not null default 0,
    taxi_id             bigint        not null references taxis (id),
    record_date         date          not null,
    pico_placa          boolean       not null default false,
    expected_production numeric(14,2) not null default 0,
    received_production numeric(14,2) not null default 0,
    driver_savings      numeric(14,2) not null default 0,
    extra_expenses      numeric(14,2) not null default 0,
    net_delivered       numeric(14,2) not null default 0,
    status              varchar(20)   not null default 'PENDIENTE',
    notes               varchar(500),
    constraint uk_daily_taxi_date unique (taxi_id, record_date),
    constraint ck_daily_amounts_non_negative check (
        expected_production >= 0 and received_production >= 0
        and driver_savings >= 0 and extra_expenses >= 0
    ),
    -- Invariante contable: lo entregado nunca es negativo
    constraint ck_daily_net_consistent check (
        net_delivered = received_production - driver_savings - extra_expenses
        and net_delivered >= 0
    ),
    constraint ck_daily_status check (status in ('PENDIENTE', 'CUMPLIDO', 'PARCIAL', 'DESCANSO'))
);

-- Los reportes filtran por rango de fechas de toda la flota
create index ix_daily_record_date on daily_records (record_date);
