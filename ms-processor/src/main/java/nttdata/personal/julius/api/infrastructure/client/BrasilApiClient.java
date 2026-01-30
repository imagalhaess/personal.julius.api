package nttdata.personal.julius.api.infrastructure.client;

import nttdata.personal.julius.api.infrastructure.client.dto.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "brasilApi", url = "${app.services.currency-api.url}")
public interface BrasilApiClient {

    @GetMapping("/cotacao/{moeda}/{data}")
    ExchangeRateResponse getQuotation(
            @PathVariable("moeda") String moeda,
            @PathVariable("data") String data
    );
}
