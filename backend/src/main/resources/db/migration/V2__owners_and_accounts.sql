-- Cuentas de propietarios: cada propietario inicia sesión con correo y contraseña
-- y solo ve sus propios conductores, taxis y registros.

create sequence user_seq start with 1 increment by 50;

create table users (
    id            bigint       primary key,
    email         varchar(160) not null,
    full_name     varchar(120) not null,
    password_hash varchar(100) not null,
    role          varchar(20)  not null default 'OWNER',
    enabled       boolean      not null default true,
    constraint uk_users_email unique (email),
    constraint ck_users_role check (role in ('OWNER'))
);

-- Si ya había datos creados antes de existir las cuentas, se conservan asignados a una
-- cuenta inhabilitada (no puede iniciar sesión). Con una base nueva esta inserción no hace nada.
insert into users (id, email, full_name, password_hash, role, enabled)
select nextval('user_seq'), 'datos-anteriores@flota.local', 'Datos anteriores', '!', 'OWNER', false
where exists (select 1 from taxis) or exists (select 1 from drivers);

alter table drivers add column owner_id bigint references users (id);
alter table taxis   add column owner_id bigint references users (id);

update drivers set owner_id = (select id from users where email = 'datos-anteriores@flota.local') where owner_id is null;
update taxis   set owner_id = (select id from users where email = 'datos-anteriores@flota.local') where owner_id is null;

alter table drivers alter column owner_id set not null;
alter table taxis   alter column owner_id set not null;

-- El documento de un conductor es único dentro de cada propietario (no en todo el sistema)
alter table drivers drop constraint uk_drivers_document;
alter table drivers add constraint uk_drivers_owner_document unique (owner_id, document_number);

-- El propietario del taxi ahora es la cuenta (owner_id): el texto libre ya no hace falta
alter table taxis drop column owner_name;

create index ix_drivers_owner on drivers (owner_id);
create index ix_taxis_owner   on taxis (owner_id);
