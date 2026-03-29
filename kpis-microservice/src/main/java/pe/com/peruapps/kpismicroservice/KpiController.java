package pe.com.peruapps.kpismicroservice;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kpis")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    @PostMapping
    public KpiResponse getKpis(@RequestBody KpiRequest request) {
        return kpiService.buildKpis(request);
    }
}
