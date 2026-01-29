package nttdata.personal.julius.api.adapter.client;

import nttdata.personal.julius.api.adapter.dto.CurrencyRateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "currencyApi", url = "${app.services.currency-api.url}")
public interface CurrencyApiClient {

    @GetMapping("/last/{currency}-BRL")
    Map<String, CurrencyRateDto> getExchangeRate(@PathVariable("currency") String currency);
}
