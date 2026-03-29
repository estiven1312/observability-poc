package pe.com.peruapps.accountableservice.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.com.peruapps.accountableservice.service.AccountableKpiService;

import java.util.List;

@RestController
public class AccountableKpiController {

    private final AccountableKpiService accountableKpiService;

    public AccountableKpiController(AccountableKpiService accountableKpiService) {
        this.accountableKpiService = accountableKpiService;
    }

    @PostMapping("/accountable-kpis")
    public List<AccountableKpiData> getAccountableKpis(@RequestBody AccountableRequest request) {
        if (request == null || request.area() == null || request.area().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "area is required");
        }

        return accountableKpiService.getKpisByArea(request.area().trim());
    }
}

