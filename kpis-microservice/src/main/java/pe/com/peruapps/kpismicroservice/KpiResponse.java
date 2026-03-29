package pe.com.peruapps.kpismicroservice;

import java.math.BigDecimal;
import java.util.List;

public record KpiResponse(
    String user,
    String area,
    List<AccountableKpiData> accountableKpis,
    List<SalesKpiData> salesKpis) {
  public record AccountableKpiData(
      String area, BigDecimal acceptedInvoices, BigDecimal rejectedInvoices) {}

  public record SalesKpiData(
      String area,
      BigDecimal totalSales,
      BigDecimal successfulSales,
      BigDecimal failedSales,
      BigDecimal convertedRatio,
      BigDecimal totalSalesGoal,
      BigDecimal rateConvertionRatioGoal) {}
}
