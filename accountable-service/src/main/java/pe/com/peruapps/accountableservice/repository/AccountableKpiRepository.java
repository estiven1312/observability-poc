package pe.com.peruapps.accountableservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.com.peruapps.accountableservice.api.AccountableKpiData;

import java.util.List;

@Repository
public class AccountableKpiRepository {

    private static final String KPI_BY_AREA_SQL = """
            SELECT
                area,
                COALESCE(SUM(CASE WHEN status = 'ACCEPTED' THEN amount ELSE 0 END), 0) AS accepted_invoices,
                COALESCE(SUM(CASE WHEN status = 'REJECTED' THEN amount ELSE 0 END), 0) AS rejected_invoices
            FROM invoices
            WHERE area = ?
            GROUP BY area
            """;

    private final JdbcTemplate jdbcTemplate;

    public AccountableKpiRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AccountableKpiData> findByArea(String area) {
        return jdbcTemplate.query(
                KPI_BY_AREA_SQL,
                (rs, rowNum) -> new AccountableKpiData(
                        rs.getString("area"),
                        rs.getBigDecimal("accepted_invoices"),
                        rs.getBigDecimal("rejected_invoices")
                ),
                area
        );
    }
}

