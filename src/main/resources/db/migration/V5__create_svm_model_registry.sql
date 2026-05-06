create table svm_model_version (
    model_version_id bigint auto_increment primary key,
    name varchar(255) not null,
    version varchar(64) not null unique,
    model_type varchar(32) not null,
    kernel_type varchar(32) not null,
    multiclass_strategy varchar(32),
    feature_set varchar(255) not null,
    image_width integer not null,
    image_height integer not null,
    patch_size integer,
    step_size integer,
    dataset_name varchar(255),
    dataset_hash varchar(128),
    train_split_ratio double,
    validation_split_ratio double,
    test_split_ratio double,
    storage_uri varchar(1000) not null,
    metrics_uri varchar(1000),
    status varchar(32) not null,
    trained_at timestamp,
    activated_at timestamp,
    created_at timestamp not null
);

create table svm_model_metric (
    metric_id bigint auto_increment primary key,
    model_version_id bigint not null,
    dataset_split varchar(32) not null,
    class_label varchar(64),
    metric_name varchar(64) not null,
    metric_value double not null,
    constraint fk_svm_model_metric_version foreign key (model_version_id) references svm_model_version (model_version_id)
);

alter table image_analysis add column model_version_id bigint;
alter table image_analysis add constraint fk_image_analysis_model_version foreign key (model_version_id) references svm_model_version (model_version_id);

insert into svm_model_version (
    name,
    version,
    model_type,
    kernel_type,
    multiclass_strategy,
    feature_set,
    image_width,
    image_height,
    patch_size,
    step_size,
    dataset_name,
    dataset_hash,
    train_split_ratio,
    validation_split_ratio,
    test_split_ratio,
    storage_uri,
    metrics_uri,
    status,
    trained_at,
    activated_at,
    created_at
) values
(
    'Full image linear SVM',
    'full-linear-legacy',
    'FULL_IMAGE',
    'LINEAR',
    'ONE_VS_ONE',
    'RAW_PIXELS_256',
    256,
    256,
    null,
    null,
    'output_dataset',
    null,
    null,
    null,
    null,
    'svm-models/svm_full_model.xml',
    'svm-models/full_metrics.json',
    'ACTIVE',
    current_timestamp,
    current_timestamp,
    current_timestamp
),
(
    'Patch based linear SVM',
    'patch-linear-legacy',
    'PATCH_BASED',
    'LINEAR',
    'ONE_VS_ONE',
    'PATCH_RAW_PIXELS_64_STEP_32',
    256,
    256,
    64,
    32,
    'output_dataset',
    null,
    null,
    null,
    null,
    'svm-models/svm_patch_model.xml',
    'svm-models/patch_metrics.json',
    'ACTIVE',
    current_timestamp,
    current_timestamp,
    current_timestamp
);

insert into svm_model_metric (model_version_id, dataset_split, class_label, metric_name, metric_value)
select model_version_id, 'LEGACY', null, 'accuracy', 0.9655629139072848
from svm_model_version where version = 'full-linear-legacy';

insert into svm_model_metric (model_version_id, dataset_split, class_label, metric_name, metric_value)
select model_version_id, 'LEGACY', null, 'accuracy', 0.23405865657521285
from svm_model_version where version = 'patch-linear-legacy';

insert into svm_model_metric (model_version_id, dataset_split, class_label, metric_name, metric_value)
select model_version_id, 'LEGACY', 'healthy', 'f1', 0.9417475728155339
from svm_model_version where version = 'full-linear-legacy';

insert into svm_model_metric (model_version_id, dataset_split, class_label, metric_name, metric_value)
select model_version_id, 'LEGACY', 'other', 'f1', 0.9360613810741688
from svm_model_version where version = 'full-linear-legacy';

insert into svm_model_metric (model_version_id, dataset_split, class_label, metric_name, metric_value)
select model_version_id, 'LEGACY', 'pneumonia', 'f1', 1.0
from svm_model_version where version = 'full-linear-legacy';

insert into svm_model_metric (model_version_id, dataset_split, class_label, metric_name, metric_value)
select model_version_id, 'LEGACY', 'emphysema', 'f1', 0.991869918699187
from svm_model_version where version = 'full-linear-legacy';

insert into svm_model_metric (model_version_id, dataset_split, class_label, metric_name, metric_value)
select model_version_id, 'LEGACY', 'fibrosis', 'f1', 0.9969788519637462
from svm_model_version where version = 'full-linear-legacy';

update image_analysis
set model_version_id = (select model_version_id from svm_model_version where version = 'full-linear-legacy')
where model_version_id is null;

create index idx_svm_model_version_type_status on svm_model_version (model_type, status);
create index idx_svm_model_metric_version_split on svm_model_metric (model_version_id, dataset_split);
create index idx_image_analysis_model_version on image_analysis (model_version_id);
