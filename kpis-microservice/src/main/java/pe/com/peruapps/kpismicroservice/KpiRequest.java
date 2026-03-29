package pe.com.peruapps.kpismicroservice;

public record KpiRequest(String user, String area) {
  public record AccountableRequest(String area) {}

  public record SalesRequest(String area) {}
}
