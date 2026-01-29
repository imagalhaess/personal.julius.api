package nttdata.personal.julius.api.application.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nttdata.personal.julius.api.infrastructure.client.BrasilApiClient;
import nttdata.personal.julius.api.infrastructure.client.MockApiClient;
import nttdata.personal.julius.api.infrastructure.client.dto.ExchangeRateResponse;
import nttdata.personal.julius.api.infrastructure.client.dto.ExternalBalanceResponse;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionCreatedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionProcessedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionResultProducer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessorService {

    private final TransactionResultProducer transactionResultProducer;
    private final MockApiClient mockApiClient;
    private final BrasilApiClient brasilApiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public void process(TransactionCreatedEvent event) {
        log.info("Processando transação: id={}, valor={}, moeda={}, data={}",
                event.transactionId(), event.amount(), event.currency(), event.createdAt());
        try {
            BigDecimal normalizedAmount = convertCurrencyToBrl(event);

            String type = event.type();
            if ("EXPENSE".equalsIgnoreCase(type)) {
                validateExternalBalance(event.userId(), normalizedAmount);
            } else {
                log.info("Tipo de transação {} não requer validação de saldo externo.", type);
            }

            transactionResultProducer.send(new TransactionProcessedEvent(event.transactionId(), true, null));
            log.info("Transação {} APROVADA!", event.transactionId());

        } catch (IllegalArgumentException e) {
            String reason = e.getMessage();
            transactionResultProducer.send(new TransactionProcessedEvent(event.transactionId(), false, reason));
            log.warn("Transação {} REJEITADA: {}", event.transactionId(), reason);

        } catch (Exception e) {
            log.error("Erro técnico ao processar transação {}", event.transactionId(), e);
            try {
                String errorMsg = "Erro técnico: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                if (errorMsg.length() > 200) errorMsg = errorMsg.substring(0, 200) + "...";
                
                transactionResultProducer.send(new TransactionProcessedEvent(event.transactionId(), false, errorMsg));
            } catch (Exception ex) {
                log.error("CRITICAL: Falha ao enviar evento de rejeição para Kafka.", ex);
                throw new RuntimeException("Falha crítica na comunicação com Kafka", ex);
            }
        }
    }

    private BigDecimal convertCurrencyToBrl(TransactionCreatedEvent event) {
        String currency = event.currency();
        BigDecimal amount = event.amount();

        if (currency == null || currency.equalsIgnoreCase("BRL")) {
            return amount;
        }

        String safeCurrency = currency.toUpperCase();
        
        LocalDateTime baseDate = event.createdAt();
        if (baseDate == null) {
            log.warn("Data da transação nula no evento recebido. Assumindo data atual para cotação.");
            baseDate = java.time.LocalDateTime.now();
        }
        
        for (int i = 0; i < 4; i++) {
            String formattedDate = baseDate.minusDays(i).format(DATE_FORMATTER);
            
            try {
                log.info("Tentativa {}: Buscando cotação na BrasilAPI para {} na data {}", i + 1, safeCurrency, formattedDate);
                ExchangeRateResponse response = brasilApiClient.getQuotation(safeCurrency, formattedDate);

                if (response != null && response.cotacoes() != null && !response.cotacoes().isEmpty()) {
                    BigDecimal rate = response.cotacoes().get(0).cotacao_compra();

                    if (rate != null) {
                        BigDecimal converted = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
                        log.info("SUCESSO: Conversão realizada usando cotação de {}: {} {} * {} = {} BRL", 
                                formattedDate, amount, safeCurrency, rate, converted);
                        return converted;
                    }
                }
                log.warn("Cotação vazia/nula para {} em {}. Tentando dia anterior...", safeCurrency, formattedDate);

            } catch (FeignException.NotFound e) {
                log.warn("Cotação não encontrada (404) para {} em {}. Tentando dia anterior...", safeCurrency, formattedDate);
            } catch (Exception e) {
                log.error("Erro técnico ao consultar BrasilAPI na data {}: {}", formattedDate, e.getMessage());
            }
        }
        throw new IllegalArgumentException("Não foi possível obter cotação para " + safeCurrency + " nos últimos 4 dias.");
    }

    private void validateExternalBalance(Long userId, BigDecimal transactionAmount) {
        try {
            List<ExternalBalanceResponse> balances = mockApiClient.getBalance(userId);

            if (balances == null || balances.isEmpty()) {
                log.warn("Usuário {} não encontrado no banco externo (MockAPI) - Lista vazia.", userId);
                throw new IllegalArgumentException("Usuário não encontrado na instituição financeira.");
            }

            BigDecimal currentBalance = balances.get(0).amount();
            log.info("Saldo Externo (BRL): {} | Valor Transação (Normalizado BRL): {}", currentBalance, transactionAmount);

            if (currentBalance.compareTo(transactionAmount) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente na conta externa.");
            }

        } catch (FeignException.NotFound e) {
            log.warn("Usuário {} não encontrado no banco externo (MockAPI) - 404.", userId);
            throw new IllegalArgumentException("Usuário não encontrado na instituição financeira.");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            log.error("Erro ao chamar MockAPI", e);
            throw new RuntimeException("Erro de comunicação com banco externo", e);
        }
    }
}
