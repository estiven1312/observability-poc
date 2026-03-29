package pe.com.peruapps.accountableservice.service;

import org.springframework.stereotype.Service;
import pe.com.peruapps.accountableservice.api.AccountableKpiData;
import pe.com.peruapps.accountableservice.repository.AccountableKpiRepository;

import java.util.List;

@Service
public class AccountableKpiService {

    private final AccountableKpiRepository accountableKpiRepository;

    public AccountableKpiService(AccountableKpiRepository accountableKpiRepository) {
        this.accountableKpiRepository = accountableKpiRepository;
    }

    public List<AccountableKpiData> getKpisByArea(String area) {
        return accountableKpiRepository.findByArea(area);
    }
}

