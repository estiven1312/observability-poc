package pe.com.peruapps.kpismicroservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
    public class KpiService {
    private final RestClient restClientKpiAccountable;
    private final RestClient restClientKpiSales;
    private final UserRepository userRepository;

    public KpiResponse buildKpis(KpiRequest request) {
        try {
            if (request == null || request.user() == null || request.user().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario es obligatorio");
            }
            if (request.area() == null || request.area().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El area es obligatoria");
            }

            String user = request.user().trim();
            String area = request.area().trim();
            if (!userRepository.existsByEmailIgnoreCaseOrNameIgnoreCase(user, user)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            simulateDelay();
            MDC.put("user", user);
            log.info("User validated: {}", user);
            KpiRequest.AccountableRequest accountableRequest = new KpiRequest.AccountableRequest(area);
            KpiRequest.SalesRequest salesRequest = new KpiRequest.SalesRequest(area);

            CompletableFuture<List<KpiResponse.AccountableKpiData>> accountableFuture =
                    CompletableFuture.supplyAsync(() -> getAccountableKpis(accountableRequest));
            CompletableFuture<List<KpiResponse.SalesKpiData>> salesFuture =
                    CompletableFuture.supplyAsync(() -> getSalesKpis(salesRequest));

            List<KpiResponse.AccountableKpiData> accountable = accountableFuture.join().stream()
                    .filter(item -> area.equalsIgnoreCase(item.area()))
                    .toList();

            List<KpiResponse.SalesKpiData> sales = salesFuture.join().stream()
                    .filter(item -> area.equalsIgnoreCase(item.area()))
                    .toList();

            return new KpiResponse(user, area, accountable, sales);
        } finally {
            MDC.clear();
        }
    }

    private void simulateDelay() {
        long delayMs = ThreadLocalRandom.current().nextLong(1500, 3001);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Proceso interrumpido", ex);
        }
    }

    public List<KpiResponse.AccountableKpiData> getAccountableKpis(KpiRequest.AccountableRequest request) {
        List<KpiResponse.AccountableKpiData> response = restClientKpiAccountable.post()
                .uri("/accountable-kpis")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<KpiResponse.AccountableKpiData>>() {});
        return response == null ? List.of() : response;
    }

    public List<KpiResponse.SalesKpiData> getSalesKpis(KpiRequest.SalesRequest request) {
        List<KpiResponse.SalesKpiData> response = restClientKpiSales.post()
                .uri("/sales-kpis")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<KpiResponse.SalesKpiData>>() {});
        return response == null ? List.of() : response;
    }
}
