package pe.com.peruapps.kpismicroservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Bean("restClientKpiAccountable")
  public RestClient restClientKpiAccountable(
      @Value("${rest-client.kpis-service.url:http://localhost:9095}") String baseUrl,
      RestClient.Builder builder) {
    return builder.clone().baseUrl(baseUrl).build();
  }

  @Bean(name = "restClientKpiSales")
  public RestClient restClientKpiSales(
      @Value("${rest-client.sales-service.url:http://localhost:9096}") String baseUrl,
      RestClient.Builder builder) {
    return builder.clone().baseUrl(baseUrl).build();
  }
}
