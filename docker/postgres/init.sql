-- ============================================================
-- FINANCE ADVISOR — SEED DATA
-- ============================================================

-- ============================================================
-- CREATE DATABASE
-- ============================================================

-- The database itself is already created by POSTGRES_DB env var
-- in docker-compose.yml, so we just need to connect to it

-- ============================================================
-- DROP TABLES (clean slate on re-seed)
-- ============================================================

DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS ai_insights CASCADE;
DROP TABLE IF EXISTS budget_rules CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;


-- ============================================================
-- CREATE TABLES
-- ============================================================

CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    currency      VARCHAR(3) DEFAULT 'INR',
    created_at    TIMESTAMP  DEFAULT NOW()
);

CREATE TABLE categories
(
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL,
    icon      VARCHAR(20),
    color_hex VARCHAR(7),
    system    BOOLEAN     NOT NULL DEFAULT FALSE,
    user_id   BIGINT REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE accounts
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)   NOT NULL,
    type       VARCHAR(20)    NOT NULL, -- SAVINGS / CASH / CREDIT_CARD / WALLET
    balance    DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency   VARCHAR(3)              DEFAULT 'INR',
    active     BOOLEAN        NOT NULL DEFAULT TRUE,
    version    BIGINT                  DEFAULT 0,
    created_at TIMESTAMP               DEFAULT NOW(),
    user_id    BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE transactions
(
    id                BIGSERIAL PRIMARY KEY,
    amount            DECIMAL(15, 2) NOT NULL,
    type              VARCHAR(10)    NOT NULL, -- DEBIT / CREDIT / TRANSFER
    description       VARCHAR(255),
    ai_category_raw   TEXT,
    categorized_by_ai BOOLEAN        NOT NULL DEFAULT FALSE,
    transfer_pair_id  BIGINT,
    occurred_at       TIMESTAMP      NOT NULL,
    created_at        TIMESTAMP               DEFAULT NOW(),
    account_id        BIGINT         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    category_id       BIGINT         REFERENCES categories (id) ON DELETE SET NULL
);

CREATE TABLE budget_rules
(
    id                  BIGSERIAL PRIMARY KEY,
    monthly_limit       DECIMAL(15, 2) NOT NULL,
    spent_this_month    DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    alert_threshold_pct INT            NOT NULL DEFAULT 80,
    rolled_over_at      TIMESTAMP,
    user_id             BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id         BIGINT         NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    UNIQUE (user_id, category_id)
);

CREATE TABLE ai_insights
(
    id           BIGSERIAL PRIMARY KEY,
    insight_text TEXT        NOT NULL,
    type         VARCHAR(20) NOT NULL, -- MONTHLY_SUMMARY / ANOMALY / SUGGESTION / BUDGET_ALERT
    month_year   VARCHAR(7),
    generated_at TIMESTAMP DEFAULT NOW(),
    user_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE audit_logs
(
    id           BIGSERIAL PRIMARY KEY,
    action       VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(50) NOT NULL,
    entity_id    BIGINT,
    performed_by BIGINT, -- no FK intentionally, must survive user deletion
    details      JSONB,
    created_at   TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_tx_account_id ON transactions (account_id);
CREATE INDEX idx_tx_category_id ON transactions (category_id);
CREATE INDEX idx_tx_occurred_at ON transactions (occurred_at);
CREATE INDEX idx_insight_user_month ON ai_insights (user_id, month_year);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_logs (performed_by);


-- ------------------------------------------------------------
-- USERS
-- password for all users is: Password@123
-- BCrypt hash generated with strength 10
-- ------------------------------------------------------------
INSERT INTO users (name, email, password_hash, currency, created_at)
VALUES ('Animesh Mondal', 'animesh@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjTsyGo2T3W', 'INR',
        NOW()),
       ('Rahul Sharma', 'rahul@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjTsyGo2T3W', 'INR',
        NOW()),
       ('Priya Mehta', 'priya@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjTsyGo2T3W', 'INR', NOW());


-- ------------------------------------------------------------
-- CATEGORIES (system = true means shared across all users)
-- ------------------------------------------------------------
INSERT INTO categories (name, icon, color_hex, system, user_id)
VALUES ('Food', '🍔', '#FF6B6B', true, NULL),
       ('Transport', '🚗', '#4ECDC4', true, NULL),
       ('Shopping', '🛍️', '#45B7D1', true, NULL),
       ('Bills', '📄', '#96CEB4', true, NULL),
       ('Entertainment', '🎬', '#FFEAA7', true, NULL),
       ('Health', '💊', '#DDA0DD', true, NULL),
       ('Salary', '💰', '#98FB98', true, NULL),
       ('Investment', '📈', '#F0E68C', true, NULL),
       ('Other', '📦', '#D3D3D3', true, NULL);


-- ------------------------------------------------------------
-- ACCOUNTS (user_id 1 = Animesh)
-- ------------------------------------------------------------
INSERT INTO accounts (name, type, balance, currency, active, version, created_at, user_id)
VALUES ('HDFC Savings', 'SAVINGS', 85000.00, 'INR', true, 0, NOW(), 1),
       ('Cash Wallet', 'CASH', 5000.00, 'INR', true, 0, NOW(), 1),
       ('ICICI Credit', 'CREDIT_CARD', 12000.00, 'INR', true, 0, NOW(), 1),
       ('PhonePe Wallet', 'WALLET', 2500.00, 'INR', true, 0, NOW(), 1);

-- Rahul's accounts (user_id 2)
INSERT INTO accounts (name, type, balance, currency, active, version, created_at, user_id)
VALUES ('SBI Savings', 'SAVINGS', 120000.00, 'INR', true, 0, NOW(), 2),
       ('Cash', 'CASH', 3000.00, 'INR', true, 0, NOW(), 2);


-- ------------------------------------------------------------
-- TRANSACTIONS for Animesh (account_id 1 = HDFC Savings)
-- ------------------------------------------------------------

-- June 2026 transactions
INSERT INTO transactions (amount, type, description, ai_category_raw, categorized_by_ai, occurred_at, created_at,
                          account_id, category_id)
VALUES (95000.00, 'CREDIT', 'June Salary', 'Salary', true, '2026-06-01 09:00:00', NOW(), 1, 7),
       (1200.00, 'DEBIT', 'Swiggy order', 'Food', true, '2026-06-02 13:30:00', NOW(), 1, 1),
       (450.00, 'DEBIT', 'Ola cab to office', 'Transport', true, '2026-06-03 08:45:00', NOW(), 1, 2),
       (3500.00, 'DEBIT', 'Amazon kurta purchase', 'Shopping', true, '2026-06-05 18:00:00', NOW(), 1, 3),
       (1800.00, 'DEBIT', 'Electricity bill', 'Bills', true, '2026-06-07 10:00:00', NOW(), 1, 4),
       (800.00, 'DEBIT', 'PVR movie tickets', 'Entertainment', true, '2026-06-08 20:00:00', NOW(), 1, 5),
       (600.00, 'DEBIT', 'Zomato dinner', 'Food', true, '2026-06-09 21:00:00', NOW(), 1, 1),
       (2500.00, 'DEBIT', 'Apollo pharmacy', 'Health', true, '2026-06-10 11:00:00', NOW(), 1, 6),
       (250.00, 'DEBIT', 'Metro recharge', 'Transport', true, '2026-06-11 09:00:00', NOW(), 1, 2),
       (5000.00, 'DEBIT', 'Mutual fund SIP', 'Investment', true, '2026-06-12 10:00:00', NOW(), 1, 8),
       (1500.00, 'DEBIT', 'Reliance Jio bill', 'Bills', true, '2026-06-13 12:00:00', NOW(), 1, 4),
       (900.00, 'DEBIT', 'Blinkit groceries', 'Food', true, '2026-06-15 17:00:00', NOW(), 1, 1),
       (350.00, 'DEBIT', 'Rapido bike ride', 'Transport', true, '2026-06-17 08:30:00', NOW(), 1, 2),
       (4200.00, 'DEBIT', 'Myntra sale haul', 'Shopping', true, '2026-06-18 16:00:00', NOW(), 1, 3),
       (1100.00, 'DEBIT', 'BookMyShow concert', 'Entertainment', true, '2026-06-20 19:00:00', NOW(), 1, 5),
       (700.00, 'DEBIT', 'Zepto instant delivery', 'Food', true, '2026-06-22 14:00:00', NOW(), 1, 1),
       (10000.00, 'DEBIT', 'Nifty 50 ETF', 'Investment', true, '2026-06-25 10:00:00', NOW(), 1, 8),
       (2200.00, 'DEBIT', 'Flipkart headphones', 'Shopping', false, '2026-06-27 15:00:00', NOW(), 1, 3),
       (400.00, 'DEBIT', 'Uber pool', 'Transport', true, '2026-06-28 09:00:00', NOW(), 1, 2),
       (1300.00, 'DEBIT', 'Lenskart glasses', 'Health', true, '2026-06-29 13:00:00', NOW(), 1, 6);

-- Cash wallet transactions (account_id 2)
INSERT INTO transactions (amount, type, description, ai_category_raw, categorized_by_ai, occurred_at, created_at,
                          account_id, category_id)
VALUES (500.00, 'DEBIT', 'Chai and snacks', 'Food', true, '2026-06-10 10:00:00', NOW(), 2, 1),
       (200.00, 'DEBIT', 'Auto rickshaw', 'Transport', true, '2026-06-14 08:00:00', NOW(), 2, 2),
       (300.00, 'DEBIT', 'Street food dinner', 'Food', true, '2026-06-19 20:00:00', NOW(), 2, 1);


-- ------------------------------------------------------------
-- BUDGET RULES for Animesh (user_id 1)
-- ------------------------------------------------------------
INSERT INTO budget_rules (monthly_limit, spent_this_month, alert_threshold_pct, user_id, category_id)
VALUES (5000.00, 4200.00, 80, 1, 1), -- Food:          ₹5000 limit, ₹4200 spent — ALERT triggered (84%)
       (2000.00, 650.00, 80, 1, 2),  -- Transport:     ₹2000 limit, ₹650 spent — safe
       (5000.00, 9900.00, 80, 1, 3), -- Shopping:      ₹5000 limit, ₹9900 spent — EXCEEDED
       (3000.00, 3300.00, 80, 1, 4), -- Bills:         ₹3000 limit, ₹3300 spent — EXCEEDED
       (2000.00, 1900.00, 80, 1, 5), -- Entertainment: ₹2000 limit, ₹1900 spent — ALERT triggered (95%)
       (3000.00, 3800.00, 80, 1, 6);
-- Health:        ₹3000 limit, ₹3800 spent — EXCEEDED


-- ------------------------------------------------------------
-- AI INSIGHTS for Animesh (user_id 1)
-- ------------------------------------------------------------
INSERT INTO ai_insights (insight_text, type, month_year, generated_at, user_id)
VALUES ('You spent ₹4,200 on Food this month, approaching your ₹5,000 budget. Swiggy and Zomato together account for ₹2,500 of this. Consider cooking at home 2–3 days a week to stay within budget.',
        'BUDGET_ALERT', '2026-06', NOW(), 1),
       ('Your Shopping spend of ₹9,900 is nearly double your ₹5,000 budget. The Myntra sale and Amazon purchases drove most of this. Consider a 30-day no-shopping rule to rebalance next month.',
        'ANOMALY', '2026-06', NOW(), 1),
       ('Great job investing ₹15,000 this month across your SIP and Nifty ETF! You are on track to build a strong passive income portfolio. Consider increasing your SIP by 10% next month.',
        'SUGGESTION', '2026-06', NOW(), 1),
       ('June Summary: Income ₹95,000 | Total Spend ₹48,050 | Invested ₹15,000 | Savings Rate 34%. Your biggest expense categories were Shopping (₹9,900), Food (₹4,200), and Investments (₹15,000).',
        'MONTHLY_SUMMARY', '2026-06', NOW(), 1);