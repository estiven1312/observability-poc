INSERT INTO sales_transactions (area, amount, status, total_sales_goal)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM seq
    WHERE n < 150
)
SELECT
    CASE MOD(n, 6)
        WHEN 0 THEN 'FINANCE'
        WHEN 1 THEN 'SALES'
        WHEN 2 THEN 'OPERATIONS'
        WHEN 3 THEN 'LOGISTICS'
        WHEN 4 THEN 'SUPPORT'
        ELSE 'HR'
    END AS area,
    ROUND((100 + (n * 17)) + (MOD(n, 13) * 0.57), 2) AS amount,
    CASE
        WHEN MOD(n, 5) IN (0, 1, 3) THEN 'SUCCESS'
        ELSE 'FAILED'
    END AS status,
    CASE MOD(n, 6)
        WHEN 0 THEN 32000.00
        WHEN 1 THEN 30000.00
        WHEN 2 THEN 28000.00
        WHEN 3 THEN 24000.00
        WHEN 4 THEN 20000.00
        ELSE 18000.00
    END AS total_sales_goal
FROM seq;
