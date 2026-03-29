package pe.com.peruapps.salesservice.service;

import org.springframework.stereotype.Service;
import pe.com.peruapps.salesservice.data.SalesKpiData;
import pe.com.peruapps.salesservice.data.SalesRequest;
import pe.com.peruapps.salesservice.repository.SalesKpiRepository;

import java.util.List;

@Service
public class SalesKpiService {

    private final SalesKpiRepository salesKpiRepository;

    public SalesKpiService(SalesKpiRepository salesKpiRepository) {
        this.salesKpiRepository = salesKpiRepository;
    }

    public List<SalesKpiData> getSalesKpis(SalesRequest request) {
        String area = request == null || request.area() == null ? null : request.area().trim();
        return salesKpiRepository.findSalesKpisByArea(area);
    }
}

