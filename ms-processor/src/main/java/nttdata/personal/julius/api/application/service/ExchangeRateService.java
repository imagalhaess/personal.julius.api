package nttdata.personal.julius.api.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nttdata.personal.julius.api.infrastructure.client.BrasilApiClient;
import nttdata.personal.julius.api.infrastructure.client.dto.ExchangeRateResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final BrasilApiClient brasilApiClient;

    @Cacheable(value = "exchangeRates", key = "#currency + '-' + #date")
    public ExchangeRateResponse getQuotation(String currency, String date) {
        log.info("Buscando cotação na BrasilAPI: {} em {}", currency, date);
        return brasilApiClient.getQuotation(currency, date);
    }

    public ConversionResult convert(BigDecimal amount, String currency) {
        if ("BRL".equalsIgnoreCase(currency)) {
            return ConversionResult.noop(amount);
        }

        try {
            String dateStr = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ExchangeRateResponse response = getQuotation(currency, dateStr);

            if (response == null || response.cotacoes() == null || response.cotacoes().isEmpty()) {
                return ConversionResult.failure("CURRENCY_CONVERSION_FAILED: Cotação não encontrada para " + currency);
            }

            BigDecimal rate = response.cotacoes().get(response.cotacoes().size() - 1).cotacao_venda();
            BigDecimal converted = amount.multiply(rate);

            log.info("Conversão: {} {} -> {} BRL (Taxa: {})", amount, currency, converted, rate);
            return ConversionResult.success(converted, rate);

        } catch (Exception e) {
            log.error("Erro ao converter moeda {}: {}", currency, e.getMessage(), e);
            return ConversionResult.failure("CURRENCY_CONVERSION_FAILED: " + e.getMessage());
        }
    }
}
