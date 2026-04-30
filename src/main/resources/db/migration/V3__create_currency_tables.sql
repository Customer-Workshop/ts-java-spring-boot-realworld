CREATE TABLE currency (
  id INTEGER PRIMARY KEY,
  currency_code VARCHAR(10) NOT NULL,
  currency_name VARCHAR(100) NOT NULL,
  country_name VARCHAR(100) NOT NULL
);

CREATE TABLE currency_rate (
  currency_code_from VARCHAR(10) NOT NULL,
  currency_code_to VARCHAR(10) NOT NULL,
  exchange_rate DOUBLE NOT NULL,
  PRIMARY KEY (currency_code_from, currency_code_to)
);

INSERT INTO currency (id, currency_code, currency_name, country_name) VALUES
(1, 'INR', 'Indian Rupees', 'INDIA'),
(2, 'USD', 'US Dollars', 'USA'),
(3, 'CAD', 'Canadian Dollars', 'CANADA'),
(4, 'EUR', 'European Dollars', 'EUROPE'),
(5, 'AUD', 'Australian Dollars', 'AUSTRALIA'),
(6, 'AED', 'UAE Dirham', 'UAE');

INSERT INTO currency_rate (currency_code_from, currency_code_to, exchange_rate) VALUES
('USD', 'INR', 80.08),
('CAD', 'INR', 61.62),
('USD', 'CAD', 1.36),
('EUR', 'INR', 93.14),
('AUD', 'INR', 56.81),
('AED', 'INR', 22.79);
