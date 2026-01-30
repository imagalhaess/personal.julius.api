package nttdata.personal.julius.api.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nttdata.personal.julius.api.common.event.TransactionCreatedEvent;
import nttdata.personal.julius.api.common.event.TransactionProcessedEvent;
import nttdata.personal.julius.api.infrastructure.client.BrasilApiClient;
import nttdata.personal.julius.api.infrastructure.client.MockApiClient;
import nttdata.personal.julius.api.infrastructure.client.dto.ExchangeRateResponse;
import nttdata.personal.julius.api.infrastructure.client.dto.ExternalBalanceResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessorService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BrasilApiClient brasilApiClient;
    private final MockApiClient mockApiClient;

    public void process(TransactionCreatedEvent event) {
        log.info("Processando transação {} para o usuário {}. Origem: {}",
                event.transactionId(), event.userId(), event.origin());

        BigDecimal convertedAmount = event.amount();
        BigDecimal exchangeRate = BigDecimal.ONE;
        boolean approved = false;
        String reason = null;

        try {
            // 1. Conversão de Moeda
            if (!"BRL".equalsIgnoreCase(event.currency())) {
                try {
                    String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    ExchangeRateResponse response = brasilApiClient.getQuotation(event.currency(), dateStr);
                    
                    if (response != null && response.cotacoes() != null && !response.cotacoes().isEmpty()) {
                        exchangeRate = response.cotacoes().get(response.cotacoes().size() - 1).cotacao_venda();
                        convertedAmount = event.amount().multiply(exchangeRate);
                        log.info("Conversão realizada: {} {} -> {} BRL (Taxa: {})", 
                                event.amount(), event.currency(), convertedAmount, exchangeRate);
                    } else {
                        throw new RuntimeException("Cotação não encontrada para " + event.currency());
                    }
                } catch (Exception e) {
                    log.error("Erro ao converter moeda: {}", e.getMessage());
                    reason = "CURRENCY_CONVERSION_FAILED";
                    sendResult(event.transactionId(), false, reason, null, null);
                    return;
                }
            }

            // 2. Validação de Saldo (apenas para EXPENSE de ACCOUNT)
            boolean isExpense = "EXPENSE".equalsIgnoreCase(event.type());
            boolean isAccount = nttdata.personal.julius.api.common.domain.TransactionOrigin.ACCOUNT.equals(event.origin());

            if (!isExpense) {
                // INCOME não precisa validar saldo
                log.info("Transação de receita (INCOME) aprovada automaticamente.");
                approved = true;
            } else if (!isAccount) {
                // CASH não precisa validar saldo
                log.info("Transação em dinheiro (CASH) aprovada automaticamente.");
                approved = true;
            } else {
                // EXPENSE + ACCOUNT: validar saldo
                log.info("Despesa de conta (ACCOUNT). Validando saldo no banco externo...");
                try {
                    List<ExternalBalanceResponse> balances = mockApiClient.getBalance(event.userId());

                    BigDecimal totalBalance = balances.stream()
                            .map(ExternalBalanceResponse::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    log.info("Saldo total encontrado para usuário {}: {}", event.userId(), totalBalance);

                    if (totalBalance.compareTo(convertedAmount) >= 0) {
                        approved = true;
                    } else {
                        reason = "INSUFFICIENT_FUNDS";
                        log.warn("Saldo insuficiente. Necessário: {}, Disponível: {}", convertedAmount, totalBalance);
                    }
                } catch (Exception e) {
                    log.error("Erro ao consultar saldo externo: {}", e.getMessage());
                    reason = "EXTERNAL_BANK_ERROR";
                }
            }

            sendResult(event.transactionId(), approved, reason, convertedAmount, exchangeRate);

        } catch (Exception e) {
            log.error("Erro fatal processando transação {}", event.transactionId(), e);
            sendResult(event.transactionId(), false, "INTERNAL_ERROR", null, null);
        }
    }

    private void sendResult(Long transactionId, boolean approved, String reason, BigDecimal convertedAmount, BigDecimal exchangeRate) {
        kafkaTemplate.send("transaction-processed", new TransactionProcessedEvent(
                transactionId,
                approved,
                reason,
                convertedAmount,
                exchangeRate
        ));
    }
}
