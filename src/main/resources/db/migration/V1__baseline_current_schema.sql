create table users (
    user_id bigint auto_increment primary key,
    user_name varchar(100),
    email varchar(100) not null unique,
    pw varchar(255) not null,
    user_role varchar(255) not null,
    creation_datetime timestamp default current_timestamp
);

create table patient (
    patient_id bigint primary key,
    birth_date date,
    gender varchar(255),
    height_cm decimal(5, 2),
    weight_kg decimal(5, 2),
    chronic_diseases text,
    allergies text,
    address varchar(255),
    last_exam_date date,
    constraint fk_patient_user foreign key (patient_id) references users (user_id) on delete cascade
);

create table doctor (
    doctor_id bigint primary key,
    position varchar(100),
    department varchar(100),
    license_number varchar(50),
    education text,
    achievements text,
    medical_institution varchar(255),
    constraint fk_doctor_user foreign key (doctor_id) references users (user_id) on delete cascade
);

create table image_file (
    image_file_id bigint auto_increment primary key,
    image_file_url varchar(255) not null,
    image_file_name varchar(255) not null,
    image_file_type varchar(100) not null,
    uploaded_at timestamp not null,
    uploaded_by_user_id bigint,
    constraint fk_image_file_uploaded_by foreign key (uploaded_by_user_id) references users (user_id) on delete cascade
);

create table image_analysis (
    image_analysis_id bigint auto_increment primary key,
    analysis_accuracy real,
    analysis_precision real,
    analysis_recall real,
    analysis_details text,
    analysis_diagnosis text,
    treatment_recommendations text,
    creation_datetime timestamp,
    analysis_status varchar(255),
    viewed boolean not null default false,
    diagnosis_class integer,
    image_file_id bigint,
    heatmap_file_id bigint,
    patient_id bigint,
    doctor_id bigint,
    constraint fk_image_analysis_image_file foreign key (image_file_id) references image_file (image_file_id),
    constraint fk_image_analysis_heatmap_file foreign key (heatmap_file_id) references image_file (image_file_id),
    constraint fk_image_analysis_patient foreign key (patient_id) references users (user_id),
    constraint fk_image_analysis_doctor foreign key (doctor_id) references users (user_id)
);

create table analysis_note (
    analysis_note_id bigint auto_increment primary key,
    note_text text not null,
    note_area_x integer,
    note_area_y integer,
    note_area_width integer,
    note_area_height integer,
    creation_datetime timestamp,
    image_analysis_id bigint,
    doctor_id bigint,
    constraint fk_analysis_note_image_analysis foreign key (image_analysis_id) references image_analysis (image_analysis_id) on delete cascade,
    constraint fk_analysis_note_doctor foreign key (doctor_id) references doctor (doctor_id) on delete set null
);

create table diagnosis_history (
    diagnosis_history_id bigint auto_increment primary key,
    image_analysis_id bigint not null,
    diagnosis_text text not null,
    changed_by_doctor_id bigint,
    change_reason text,
    change_datetime timestamp,
    analysis_details text,
    treatment_recommendations text,
    constraint fk_diagnosis_history_image_analysis foreign key (image_analysis_id) references image_analysis (image_analysis_id),
    constraint fk_diagnosis_history_doctor foreign key (changed_by_doctor_id) references doctor (doctor_id)
);

create index idx_image_analysis_patient on image_analysis (patient_id);
create index idx_image_analysis_doctor on image_analysis (doctor_id);
create index idx_image_analysis_status on image_analysis (analysis_status);
create index idx_analysis_note_image_analysis on analysis_note (image_analysis_id);
create index idx_diagnosis_history_image_analysis on diagnosis_history (image_analysis_id);
