create table analysis_comparison (
    analysis_comparison_id bigint auto_increment primary key,
    hospital_id bigint not null,
    treatment_id bigint not null,
    from_analysis_id bigint not null,
    to_analysis_id bigint not null,
    diagnosis_class_from integer,
    diagnosis_class_to integer,
    diagnosis_changed boolean not null,
    accuracy_delta real,
    precision_delta real,
    recall_delta real,
    doctor_notes text,
    created_by_user_id bigint not null,
    created_at timestamp not null,
    constraint fk_analysis_comparison_hospital foreign key (hospital_id) references hospital (hospital_id),
    constraint fk_analysis_comparison_treatment foreign key (treatment_id) references treatment (treatment_id),
    constraint fk_analysis_comparison_from_analysis foreign key (from_analysis_id) references image_analysis (image_analysis_id),
    constraint fk_analysis_comparison_to_analysis foreign key (to_analysis_id) references image_analysis (image_analysis_id),
    constraint fk_analysis_comparison_created_by foreign key (created_by_user_id) references users (user_id)
);

create table treatment_conclusion (
    treatment_conclusion_id bigint auto_increment primary key,
    hospital_id bigint not null,
    treatment_id bigint not null unique,
    conclusion_text text not null,
    recommendations text,
    created_by_user_id bigint not null,
    updated_by_user_id bigint not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_treatment_conclusion_hospital foreign key (hospital_id) references hospital (hospital_id),
    constraint fk_treatment_conclusion_treatment foreign key (treatment_id) references treatment (treatment_id),
    constraint fk_treatment_conclusion_created_by foreign key (created_by_user_id) references users (user_id),
    constraint fk_treatment_conclusion_updated_by foreign key (updated_by_user_id) references users (user_id)
);

create index idx_analysis_comparison_treatment_created on analysis_comparison (treatment_id, created_at);
create index idx_analysis_comparison_hospital_treatment on analysis_comparison (hospital_id, treatment_id);
create index idx_treatment_conclusion_hospital on treatment_conclusion (hospital_id);
