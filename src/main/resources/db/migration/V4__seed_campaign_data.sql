-- Grant Marketing entitlement to existing users
INSERT INTO user_entitlements (id, user_id, entitlement) VALUES
('ent-1', 'user-1', 'Marketing'),
('ent-2', 'user-2', 'Marketing');

-- Sample campaigns
INSERT INTO campaigns (id, name, status, target_audience_segment, start_date, end_date,
    message_title, message_body, message_image_url, message_cta_text,
    fulfillment_action_type, created_by, archived, created_at, updated_at)
VALUES
('campaign-1', 'Spring Savings Promotion', 'ACTIVE', 'High-Value Customers',
    datetime('now', '-5 days'), datetime('now', '+25 days'),
    'Exclusive Spring Savings!', 'Enjoy up to 30% off on selected products this spring season. Limited time offer for our valued customers.',
    NULL, 'Shop Now', 'ACCEPT', 'user-1', 0, datetime('now', '-5 days'), datetime('now', '-5 days')),

('campaign-2', 'New Customer Welcome', 'DRAFT', 'New Signups',
    datetime('now', '+5 days'), datetime('now', '+35 days'),
    'Welcome to Our Platform!', 'As a new member, enjoy a special welcome package with exclusive benefits and early access to new features.',
    NULL, 'Claim Your Reward', 'ACCEPT', 'user-1', 0, datetime('now', '-3 days'), datetime('now', '-3 days')),

('campaign-3', 'Year-End Clearance', 'ENDED', 'All Customers',
    datetime('now', '-60 days'), datetime('now', '-10 days'),
    'Year-End Clearance Sale', 'Massive discounts across all categories. Stock is limited—grab your favorites before they are gone!',
    NULL, 'Browse Deals', 'ACCEPT', 'user-2', 0, datetime('now', '-60 days'), datetime('now', '-10 days')),

('campaign-4', 'Loyalty Rewards Boost', 'PAUSED', 'Loyalty Members',
    datetime('now', '-10 days'), datetime('now', '+20 days'),
    'Double Points Weekend!', 'Earn double loyalty points on every purchase this weekend. Stack your rewards faster!',
    NULL, 'Start Earning', 'REMIND_LATER', 'user-2', 0, datetime('now', '-10 days'), datetime('now', '-10 days')),

('campaign-5', 'Summer Flash Sale', 'ACTIVE', 'Millennials 25-35',
    datetime('now', '-2 days'), datetime('now', '+28 days'),
    'Flash Sale: 48 Hours Only!', 'Do not miss our biggest flash sale of the summer. New deals drop every 6 hours.',
    NULL, 'View Deals', 'DECLINE', 'user-1', 0, datetime('now', '-2 days'), datetime('now', '-2 days'));

-- Sample campaign decisions for analytics
INSERT INTO campaign_decisions (id, campaign_id, user_id, decision, user_segment, user_age_group, user_region, decided_at)
VALUES
('dec-1', 'campaign-1', 'user-1', 'ACCEPTED', 'High-Value Customers', '30-40', 'North America', datetime('now', '-4 days')),
('dec-2', 'campaign-1', 'user-2', 'ACCEPTED', 'High-Value Customers', '25-30', 'Europe', datetime('now', '-4 days')),
('dec-3', 'campaign-1', 'user-3', 'DECLINED', 'High-Value Customers', '30-40', 'North America', datetime('now', '-3 days')),
('dec-4', 'campaign-3', 'user-1', 'ACCEPTED', 'All Customers', '30-40', 'North America', datetime('now', '-50 days')),
('dec-5', 'campaign-3', 'user-2', 'CLICKED_UNFINISHED', 'All Customers', '25-30', 'Europe', datetime('now', '-45 days')),
('dec-6', 'campaign-3', 'user-3', 'ACCEPTED', 'All Customers', '40-50', 'Asia Pacific', datetime('now', '-40 days')),
('dec-7', 'campaign-5', 'user-1', 'ACCEPTED', 'Millennials 25-35', '25-30', 'North America', datetime('now', '-1 days')),
('dec-8', 'campaign-5', 'user-2', 'DECLINED', 'Millennials 25-35', '30-40', 'Europe', datetime('now', '-1 days')),
('dec-9', 'campaign-5', 'user-3', 'CLICKED_UNFINISHED', 'Millennials 25-35', '25-30', 'Asia Pacific', datetime('now', '-1 days')),
('dec-10', 'campaign-1', 'user-1', 'ACCEPTED', 'High-Value Customers', '25-30', 'North America', datetime('now', '-2 days')),
('dec-11', 'campaign-1', 'user-2', 'CLICKED_UNFINISHED', 'High-Value Customers', '30-40', 'Europe', datetime('now', '-2 days')),
('dec-12', 'campaign-5', 'user-1', 'ACCEPTED', 'Millennials 25-35', '25-30', 'North America', datetime('now'));
