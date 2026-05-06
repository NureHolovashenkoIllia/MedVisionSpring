create table analysis_job (
    analysis_job_id bigint auto_increment primary key,
    image_analysis_id bigint,
    hospital_id bigint,
    treatment_id bigint,
    requested_by_user_id bigint not null,
    status varchar(32) not null,
    request_payload text,
    response_payload text,
    error_message text,
    started_at timestamp,
    finished_at timestamp,
    created_at timestamp not null,
    constraint fk_analysis_job_image_analysis foreign key (image_analysis_id) references image_analysis (image_analysis_id),
    constraint fk_analysis_job_hospital foreign key (hospital_id) references hospital (hospital_id),
    constraint fk_analysis_job_treatment foreign key (treatment_id) references treatment (treatment_id),
    constraint fk_analysis_job_requested_by foreign key (requested_by_user_id) references users (user_id)
);

alter table image_analysis add column analysis_job_id bigint;
alter table image_analysis add constraint fk_image_analysis_analysis_job foreign key (analysis_job_id) references analysis_job (analysis_job_id);

insert into analysis_job (
    image_analysis_id,
    hospital_id,
    treatment_id,
    requested_by_user_id,
    status,
    request_payload,
    response_payload,
    error_message,
    started_at,
    finished_at,
    created_at
)
select
    image_analysis_id,
    hospital_id,
    treatment_id,
    coalesce(doctor_id, patient_id),
    'COMPLETED',
    'legacy analysis backfill',
    'legacy analysis existed before job support',
    null,
    creation_datetime,
    creation_datetime,
    coalesce(creation_datetime, current_timestamp)
from image_analysis
where coalesce(doctor_id, patient_id) is not null;

update image_analysis
set analysis_job_id = (
    select min(analysis_job.analysis_job_id)
    from analysis_job
    where analysis_job.image_analysis_id = image_analysis.image_analysis_id
)
where analysis_job_id is null;

create index idx_analysis_job_hospital_status on analysis_job (hospital_id, status);
create index idx_analysis_job_treatment_status on analysis_job (treatment_id, status);
create index idx_image_analysis_job on image_analysis (analysis_job_id);
