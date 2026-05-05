-- V6: Industry-standard campaign management features
-- A/B Testing, Tags, Multi-Channel, Priority, Audit Trail

-- New columns on campaigns table
ALTER TABLE campaigns ADD COLUMN channel varchar(50) default 'IN_APP';
ALTER TABLE campaigns ADD COLUMN priority integer default 5;
ALTER TABLE campaigns ADD COLUMN tags varchar(1024);
ALTER TABLE campaigns ADD COLUMN ab_test_enabled integer default 0;

-- A/B Test Variants
create table campaign_ab_variants (
  id varchar(255) primary key,
  campaign_id varchar(255) not null,
  variant_name varchar(100) not null,
  split_percentage integer not null default 50,
  message_title varchar(255),
  message_body text,
  message_cta_text varchar(255),
  message_image_url varchar(1024),
  impressions integer not null default 0,
  conversions integer not null default 0,
  is_winner integer not null default 0,
  created_at timestamp not null default current_timestamp,
  foreign key (campaign_id) references campaigns(id)
);

-- Campaign Tags (normalized)
create table campaign_tags (
  id varchar(255) primary key,
  campaign_id varchar(255) not null,
  tag varchar(100) not null,
  foreign key (campaign_id) references campaigns(id)
);

-- Audit Trail
create table campaign_audit_log (
  id varchar(255) primary key,
  campaign_id varchar(255) not null,
  user_id varchar(255) not null,
  action varchar(50) not null,
  field_name varchar(100),
  old_value text,
  new_value text,
  timestamp timestamp not null default current_timestamp,
  foreign key (campaign_id) references campaigns(id)
);

-- Indexes
create index idx_ab_variants_campaign on campaign_ab_variants(campaign_id);
create index idx_campaign_tags_campaign on campaign_tags(campaign_id);
create index idx_campaign_tags_tag on campaign_tags(tag);
create index idx_audit_log_campaign on campaign_audit_log(campaign_id);
create index idx_audit_log_timestamp on campaign_audit_log(timestamp);
create index idx_campaigns_priority on campaigns(priority);
create index idx_campaigns_channel on campaigns(channel);

-- Seed data: tags for existing campaigns
INSERT INTO campaign_tags (id, campaign_id, tag) VALUES
('tag-1', 'campaign-1', 'rewards'),
('tag-2', 'campaign-1', 'premium'),
('tag-3', 'campaign-2', 'onboarding'),
('tag-4', 'campaign-2', 'welcome'),
('tag-5', 'campaign-3', 'seasonal'),
('tag-6', 'campaign-3', 'promotion'),
('tag-7', 'campaign-4', 'loyalty'),
('tag-8', 'campaign-4', 'retention'),
('tag-9', 'campaign-5', 'flash-sale'),
('tag-10', 'campaign-5', 'engagement');

-- Update existing campaigns with channel and priority
UPDATE campaigns SET channel = 'IN_APP', priority = 8, tags = 'rewards,premium' WHERE id = 'campaign-1';
UPDATE campaigns SET channel = 'EMAIL', priority = 9, tags = 'onboarding,welcome' WHERE id = 'campaign-2';
UPDATE campaigns SET channel = 'PUSH', priority = 5, tags = 'seasonal,promotion' WHERE id = 'campaign-3';
UPDATE campaigns SET channel = 'SMS', priority = 7, tags = 'loyalty,retention' WHERE id = 'campaign-4';
UPDATE campaigns SET channel = 'IN_APP', priority = 10, tags = 'flash-sale,engagement' WHERE id = 'campaign-5';

-- Seed A/B test variant for campaign-1
UPDATE campaigns SET ab_test_enabled = 1 WHERE id = 'campaign-1';
INSERT INTO campaign_ab_variants (id, campaign_id, variant_name, split_percentage, message_title, message_body, message_cta_text, impressions, conversions, created_at) VALUES
('abv-1', 'campaign-1', 'Control', 50, 'Upgrade to Premium', 'Get exclusive rewards and benefits with our premium tier.', 'Upgrade Now', 1200, 340, datetime('now', '-10 days')),
('abv-2', 'campaign-1', 'Variant B', 50, 'Unlock Premium Benefits Today', 'Join thousands who enjoy premium rewards, higher limits, and priority support.', 'Start Premium', 1180, 410, datetime('now', '-10 days'));

-- Seed audit log entries
INSERT INTO campaign_audit_log (id, campaign_id, user_id, action, field_name, old_value, new_value, timestamp) VALUES
('audit-1', 'campaign-1', 'user-1', 'CREATED', null, null, null, datetime('now', '-30 days')),
('audit-2', 'campaign-1', 'user-1', 'STATUS_CHANGE', 'status', 'DRAFT', 'ACTIVE', datetime('now', '-28 days')),
('audit-3', 'campaign-1', 'user-1', 'FIELD_UPDATE', 'messageBody', 'Original body text', 'Get exclusive rewards and benefits with our premium tier.', datetime('now', '-25 days')),
('audit-4', 'campaign-2', 'user-1', 'CREATED', null, null, null, datetime('now', '-20 days')),
('audit-5', 'campaign-2', 'user-1', 'STATUS_CHANGE', 'status', 'DRAFT', 'ACTIVE', datetime('now', '-18 days')),
('audit-6', 'campaign-3', 'user-1', 'CREATED', null, null, null, datetime('now', '-15 days')),
('audit-7', 'campaign-3', 'user-1', 'STATUS_CHANGE', 'status', 'DRAFT', 'ACTIVE', datetime('now', '-14 days')),
('audit-8', 'campaign-3', 'user-1', 'STATUS_CHANGE', 'status', 'ACTIVE', 'ENDED', datetime('now', '-5 days'));
