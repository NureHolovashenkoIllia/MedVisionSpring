create table hospital (
    hospital_id bigint auto_increment primary key,
    name varchar(255) not null,
    legal_name varchar(255),
    code varchar(64) unique,
    address varchar(500),
    phone varchar(50),
    email varchar(100),
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp
);

create table user_hospital_membership (
    membership_id bigint auto_increment primary key,
    user_id bigint not null,
    hospital_id bigint not null,
    hospital_role varchar(32) not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp,
    constraint fk_membership_user foreign key (user_id) references users (user_id),
    constraint fk_membership_hospital foreign key (hospital_id) references hospital (hospital_id),
    constraint uq_membership_user_hospital_role unique (user_id, hospital_id, hospital_role)
);

create index idx_membership_user_status on user_hospital_membership (user_id, status);
create index idx_membership_hospital_role on user_hospital_membership (hospital_id, hospital_role);

insert into hospital (name, legal_name, code, address, phone, email, status, created_at, updated_at)
values ('Legacy MedVision Hospital', 'Legacy MedVision Hospital', 'LEGACY', null, null, null, 'ACTIVE', current_timestamp, null);

insert into user_hospital_membership (user_id, hospital_id, hospital_role, status, created_at, updated_at)
select
    user_id,
    (select hospital_id from hospital where code = 'LEGACY'),
    case
        when user_role = 'DOCTOR' then 'DOCTOR'
        when user_role = 'PATIENT' then 'PATIENT'
        else 'HOSPITAL_ADMIN'
    end,
    'ACTIVE',
    current_timestamp,
    null
from users;
