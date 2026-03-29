package pe.com.peruapps.salesservice.data;

import java.math.BigDecimal;

public record SalesKpiData(
        String area,
        BigDecimal totalSales,
        BigDecimal successfulSales,
        BigDecimal failedSales,
        BigDecimal convertedRatio,
        BigDecimal totalSalesGoal,
        BigDecimal rateConvertionRatioGoal
) {
}

