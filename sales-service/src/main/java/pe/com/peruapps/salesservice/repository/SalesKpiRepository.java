package pe.com.peruapps.salesservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.com.peruapps.salesservice.data.SalesKpiData;

import java.util.List;

@Repository
public class SalesKpiRepository {

    private final JdbcTemplate jdbcTemplate;

    public SalesKpiRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SalesKpiData> findSalesKpisByArea(String area) {
        String sql = """
                SELECT
                    st.area AS area,
                    COALESCE(SUM(st.amount), 0) AS total_sales,
                    COALESCE(SUM(CASE WHEN st.status = 'SUCCESS' THEN st.amount ELSE 0 END), 0) AS successful_sales,
                    COALESCE(SUM(CASE WHEN st.status = 'FAILED' THEN st.amount ELSE 0 END), 0) AS failed_sales,
                    CASE
                        WHEN COALESCE(SUM(st.amount), 0) = 0 THEN 0
                        ELSE ROUND(
                            COALESCE(SUM(CASE WHEN st.status = 'SUCCESS' THEN st.amount ELSE 0 END), 0) / SUM(st.amount),
                            4
                        )
                    END AS converted_ratio,
                    COALESCE(MAX(st.total_sales_goal), 0) AS total_sales_goal,
                    CASE
                        WHEN COALESCE(MAX(st.total_sales_goal), 0) = 0 THEN 0
                        ELSE ROUND(
                            COALESCE(SUM(CASE WHEN st.status = 'SUCCESS' THEN st.amount ELSE 0 END), 0) / MAX(st.total_sales_goal),
                            4
                        )
                    END AS rate_convertion_ratio_goal
                FROM sales_transactions st
                WHERE (? IS NULL OR ? = '' OR st.area = ?)
                GROUP BY st.area
                ORDER BY st.area
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new SalesKpiData(
                        rs.getString("area"),
                        rs.getBigDecimal("total_sales"),
                        rs.getBigDecimal("successful_sales"),
                        rs.getBigDecimal("failed_sales"),
                        rs.getBigDecimal("converted_ratio"),
                        rs.getBigDecimal("total_sales_goal"),
                        rs.getBigDecimal("rate_convertion_ratio_goal")
                ),
                area,
                area,
                area
        );
    }
}

