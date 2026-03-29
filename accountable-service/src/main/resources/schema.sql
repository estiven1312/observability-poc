CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    area VARCHAR(100) NOT NULL,
    amount NUMERIC(15,2) NOT NULL CHECK (amount >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACCEPTED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_invoices_area ON invoices(area);

