-- Campaign Management System tables

create table campaigns (
  id varchar(255) primary key,
  name varchar(255) not null,
  status varchar(50) not null default 'DRAFT',
  target_audience_segment varchar(255),
  start_date timestamp,
  end_date timestamp,
  message_title varchar(255),
  message_body text,
  message_image_url varchar(1024),
  message_cta_text varchar(255),
  fulfillment_action_type varchar(50) not null default 'ACCEPT',
  created_by varchar(255) not null,
  archived integer not null default 0,
  created_at timestamp not null,
  updated_at timestamp not null default current_timestamp
);

create table campaign_decisions (
  id varchar(255) primary key,
  campaign_id varchar(255) not null,
  user_id varchar(255) not null,
  decision varchar(50) not null,
  user_segment varchar(255),
  user_age_group varchar(50),
  user_region varchar(255),
  decided_at timestamp not null,
  foreign key (campaign_id) references campaigns(id)
);

create table user_entitlements (
  id varchar(255) primary key,
  user_id varchar(255) not null,
  entitlement varchar(255) not null,
  granted_at timestamp not null default current_timestamp,
  unique(user_id, entitlement)
);

create index idx_campaigns_status on campaigns(status);
create index idx_campaigns_created_by on campaigns(created_by);
create index idx_campaign_decisions_campaign_id on campaign_decisions(campaign_id);
create index idx_campaign_decisions_decision on campaign_decisions(decision);
create index idx_user_entitlements_user_id on user_entitlements(user_id);
