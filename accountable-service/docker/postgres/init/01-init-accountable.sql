-- Container startup script (runs only when PGDATA is empty).
-- Includes schema creation and a large initial seed (>100 rows).

CREATE SCHEMA IF NOT EXISTS accounting;

CREATE TABLE IF NOT EXISTS accounting.invoices (
    id BIGSERIAL PRIMARY KEY,
    area VARCHAR(100) NOT NULL,
    amount NUMERIC(15,2) NOT NULL CHECK (amount >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACCEPTED', 'REJECTED')),
    issued_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_accounting_invoices_area ON accounting.invoices(area);
CREATE INDEX IF NOT EXISTS idx_accounting_invoices_status ON accounting.invoices(status);

-- Keep compatibility with app query that reads from public.invoices.
CREATE OR REPLACE VIEW public.invoices AS
SELECT id, area, amount, status
FROM accounting.invoices;

-- Seed deterministic rows with >100 inserts (250 rows).
INSERT INTO accounting.invoices (area, amount, status, issued_at)
SELECT
    CASE (n % 6)
        WHEN 0 THEN 'FINANCE'
        WHEN 1 THEN 'SALES'
        WHEN 2 THEN 'OPERATIONS'
        WHEN 3 THEN 'LOGISTICS'
        WHEN 4 THEN 'SUPPORT'
        ELSE 'HR'
    END AS area,
    ROUND((100 + ((n * 37) % 5000))::NUMERIC, 2) AS amount,
    CASE WHEN n % 4 = 0 THEN 'REJECTED' ELSE 'ACCEPTED' END AS status,
    NOW() - ((n % 90) || ' days')::INTERVAL AS issued_at
FROM generate_series(1, 250) AS s(n);

-- Extra explicit data for quick manual checks.
INSERT INTO accounting.invoices (area, amount, status, issued_at) VALUES
('FINANCE', 1000.00, 'ACCEPTED', NOW()),
('FINANCE', 200.50, 'ACCEPTED', NOW()),
('FINANCE', 300.00, 'REJECTED', NOW()),
('SALES', 1500.00, 'ACCEPTED', NOW()),
('SALES', 100.00, 'REJECTED', NOW());

