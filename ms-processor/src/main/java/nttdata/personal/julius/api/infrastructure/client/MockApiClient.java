package nttdata.personal.julius.api.infrastructure.client;

import nttdata.personal.julius.api.infrastructure.client.dto.ExternalBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mockApi", url = "${app.services.mock-api.url}")
public interface MockApiClient {

    @GetMapping("/balances")
    List<ExternalBalanceResponse> getBalance(@RequestParam("accountId") Long accountId);
}
