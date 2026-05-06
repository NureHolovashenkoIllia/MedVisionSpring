create table treatment (
    treatment_id bigint auto_increment primary key,
    hospital_id bigint not null,
    patient_id bigint not null,
    primary_doctor_id bigint not null,
    title varchar(255) not null,
    description text,
    status varchar(32) not null,
    opened_at timestamp not null,
    closed_at timestamp,
    close_reason text,
    created_by_user_id bigint not null,
    created_at timestamp not null,
    updated_at timestamp,
    constraint fk_treatment_hospital foreign key (hospital_id) references hospital (hospital_id),
    constraint fk_treatment_patient foreign key (patient_id) references users (user_id),
    constraint fk_treatment_primary_doctor foreign key (primary_doctor_id) references users (user_id),
    constraint fk_treatment_created_by foreign key (created_by_user_id) references users (user_id)
);

create table treatment_status_history (
    history_id bigint auto_increment primary key,
    treatment_id bigint not null,
    from_status varchar(32),
    to_status varchar(32) not null,
    changed_by_user_id bigint not null,
    reason text,
    changed_at timestamp not null,
    constraint fk_treatment_status_history_treatment foreign key (treatment_id) references treatment (treatment_id),
    constraint fk_treatment_status_history_changed_by foreign key (changed_by_user_id) references users (user_id)
);

alter table image_analysis add column hospital_id bigint;
alter table image_analysis add column treatment_id bigint;
alter table image_file add column hospital_id bigint;
alter table image_file add column treatment_id bigint;

alter table image_analysis add constraint fk_image_analysis_hospital foreign key (hospital_id) references hospital (hospital_id);
alter table image_analysis add constraint fk_image_analysis_treatment foreign key (treatment_id) references treatment (treatment_id);
alter table image_file add constraint fk_image_file_hospital foreign key (hospital_id) references hospital (hospital_id);
alter table image_file add constraint fk_image_file_treatment foreign key (treatment_id) references treatment (treatment_id);

update image_analysis
set hospital_id = (select hospital_id from hospital where code = 'LEGACY')
where hospital_id is null;

update image_file
set hospital_id = (select hospital_id from hospital where code = 'LEGACY')
where hospital_id is null;

insert into treatment (
    hospital_id,
    patient_id,
    primary_doctor_id,
    title,
    description,
    status,
    opened_at,
    closed_at,
    close_reason,
    created_by_user_id,
    created_at,
    updated_at
)
select distinct
    hospital_id,
    patient_id,
    doctor_id,
    'Legacy treatment',
    'Backfilled treatment for analyses created before treatment case support.',
    'OPEN',
    current_timestamp,
    null,
    null,
    doctor_id,
    current_timestamp,
    null
from image_analysis
where patient_id is not null and doctor_id is not null and hospital_id is not null;

update image_analysis
set treatment_id = (
    select min(t.treatment_id)
    from treatment t
    where t.hospital_id = image_analysis.hospital_id
      and t.patient_id = image_analysis.patient_id
      and t.primary_doctor_id = image_analysis.doctor_id
)
where treatment_id is null and patient_id is not null and doctor_id is not null;

update image_file
set treatment_id = (
    select min(ia.treatment_id)
    from image_analysis ia
    where ia.image_file_id = image_file.image_file_id or ia.heatmap_file_id = image_file.image_file_id
)
where treatment_id is null;

create index idx_treatment_hospital_patient_status on treatment (hospital_id, patient_id, status);
create index idx_treatment_hospital_doctor_status on treatment (hospital_id, primary_doctor_id, status);
create index idx_treatment_patient_opened_at on treatment (patient_id, opened_at);
create index idx_treatment_status_history_treatment on treatment_status_history (treatment_id);
create index idx_image_analysis_hospital on image_analysis (hospital_id);
create index idx_image_analysis_treatment on image_analysis (treatment_id);
create index idx_image_file_hospital on image_file (hospital_id);
create index idx_image_file_treatment on image_file (treatment_id);
