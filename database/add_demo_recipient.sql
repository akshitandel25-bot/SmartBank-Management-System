-- Add the demo recipient needed by the SmartBank transfer screen.
-- Run this once in phpMyAdmin after selecting the `smartbank` database.
INSERT IGNORE INTO customers (customer_id, full_name, email, phone)
VALUES (2, 'Demo Recipient', 'recipient@smartbank.com', '9999999998');

INSERT IGNORE INTO accounts (account_id, customer_id, account_number, balance, account_type)
VALUES (2, 2, 'SB100002', 10000.00, 'Savings');
