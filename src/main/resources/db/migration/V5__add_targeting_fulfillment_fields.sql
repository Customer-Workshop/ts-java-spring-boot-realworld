-- Story 3: Personalization & Targeting fields
-- Story 4: End-User Fulfillment fields

ALTER TABLE campaigns ADD COLUMN display_placement varchar(100) default 'POST_LOGIN';
ALTER TABLE campaigns ADD COLUMN frequency_cap_type varchar(50) default 'ONCE_PER_CAMPAIGN';
ALTER TABLE campaigns ADD COLUMN frequency_cap_max_impressions integer default 1;
ALTER TABLE campaigns ADD COLUMN delivery_start_time varchar(10);
ALTER TABLE campaigns ADD COLUMN delivery_end_time varchar(10);
ALTER TABLE campaigns ADD COLUMN personalization_tokens text;
ALTER TABLE campaigns ADD COLUMN remind_later_deferral_days integer default 1;
ALTER TABLE campaigns ADD COLUMN fulfillment_workflow_url varchar(1024);
ALTER TABLE campaigns ADD COLUMN decline_suppression integer default 1;
ALTER TABLE campaigns ADD COLUMN confirmation_message varchar(500) default 'Thank you for your response!';
ALTER TABLE campaigns ADD COLUMN audience_rules text;

-- Update seed data with targeting configuration
UPDATE campaigns SET
    display_placement = 'POST_LOGIN',
    frequency_cap_type = 'ONCE_PER_DAY',
    frequency_cap_max_impressions = 3,
    delivery_start_time = '08:00',
    delivery_end_time = '20:00',
    remind_later_deferral_days = 3,
    decline_suppression = 1,
    confirmation_message = 'Thank you! Your response has been recorded.',
    audience_rules = '{"attributes":["accountType","segment"]}'
WHERE id = 'campaign-1';

UPDATE campaigns SET
    display_placement = 'POST_LOGIN',
    frequency_cap_type = 'ONCE_PER_CAMPAIGN',
    frequency_cap_max_impressions = 1,
    delivery_start_time = '09:00',
    delivery_end_time = '18:00',
    remind_later_deferral_days = 7,
    decline_suppression = 1,
    confirmation_message = 'Welcome aboard! Check your inbox for your welcome package.',
    audience_rules = '{"attributes":["accountType"]}'
WHERE id = 'campaign-2';

UPDATE campaigns SET
    display_placement = 'LOGGED_OFF',
    frequency_cap_type = 'ONCE_PER_SESSION',
    frequency_cap_max_impressions = 5,
    remind_later_deferral_days = 1,
    decline_suppression = 1,
    confirmation_message = 'Thanks for checking out our deals!'
WHERE id = 'campaign-3';

UPDATE campaigns SET
    display_placement = 'POST_LOGIN',
    frequency_cap_type = 'ONCE_PER_DAY',
    frequency_cap_max_impressions = 2,
    delivery_start_time = '10:00',
    delivery_end_time = '22:00',
    remind_later_deferral_days = 2,
    decline_suppression = 0,
    confirmation_message = 'Points are on their way!',
    personalization_tokens = '[{"token":"{{firstName}}","field":"First Name"},{"token":"{{pointsBalance}}","field":"Points Balance"}]'
WHERE id = 'campaign-4';

UPDATE campaigns SET
    display_placement = 'POST_LOGIN',
    frequency_cap_type = 'ONCE_PER_SESSION',
    frequency_cap_max_impressions = 3,
    delivery_start_time = '06:00',
    delivery_end_time = '23:59',
    remind_later_deferral_days = 1,
    decline_suppression = 1,
    confirmation_message = 'Happy shopping!',
    fulfillment_workflow_url = 'https://api.example.com/flash-sale/enroll'
WHERE id = 'campaign-5';

-- Add some REMIND_LATER decisions to seed data
INSERT INTO campaign_decisions (id, campaign_id, user_id, decision, user_segment, user_age_group, user_region, decided_at)
VALUES
('dec-13', 'campaign-1', 'user-3', 'REMIND_LATER', 'High-Value Customers', '40-50', 'North America', datetime('now', '-1 days')),
('dec-14', 'campaign-4', 'user-1', 'REMIND_LATER', 'Loyalty Members', '25-30', 'Europe', datetime('now', '-5 days')),
('dec-15', 'campaign-5', 'user-2', 'REMIND_LATER', 'Millennials 25-35', '30-40', 'Asia Pacific', datetime('now'));
