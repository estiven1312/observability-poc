package pe.com.peruapps.salesservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.com.peruapps.salesservice.data.SalesKpiData;
import pe.com.peruapps.salesservice.data.SalesRequest;
import pe.com.peruapps.salesservice.service.SalesKpiService;

import java.util.List;

@RestController
public class SalesKpiController {

    private final SalesKpiService salesKpiService;

    public SalesKpiController(SalesKpiService salesKpiService) {
        this.salesKpiService = salesKpiService;
    }

    @PostMapping("/sales-kpis")
    public List<SalesKpiData> getSalesKpis(@RequestBody SalesRequest request) {
        return salesKpiService.getSalesKpis(request);
    }
}

