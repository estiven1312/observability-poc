package pe.com.peruapps.accountableservice.api;

import java.math.BigDecimal;

public record AccountableKpiData(
        String area,
        BigDecimal acceptedInvoices,
        BigDecimal rejectedInvoices
) {
}

