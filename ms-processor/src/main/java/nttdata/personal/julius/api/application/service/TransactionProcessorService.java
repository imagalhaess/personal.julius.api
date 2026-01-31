package nttdata.personal.julius.api.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nttdata.personal.julius.api.common.domain.TransactionOrigin;
import nttdata.personal.julius.api.common.event.TransactionCreatedEvent;
import nttdata.personal.julius.api.common.event.TransactionProcessedEvent;
import nttdata.personal.julius.api.infrastructure.client.MockApiClient;
import nttdata.personal.julius.api.infrastructure.client.dto.ExternalBalanceResponse;
import nttdata.personal.julius.api.infrastructure.messaging.DlqProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessorService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ExchangeRateService exchangeRateService;
    private final MockApiClient mockApiClient;
    private final DlqProducer dlqProducer;

    public void process(TransactionCreatedEvent event) {
        log.info("Processando transação {} para usuário {}. Origem: {}",
                event.transactionId(), event.userId(), event.origin());

        try {
            ConversionResult conversion = exchangeRateService.convert(event.amount(), event.currency());

            if (!conversion.success()) {
                dlqProducer.send(event, conversion.reason());
                sendResult(event.transactionId(), false, conversion.reason(), null, null);
                return;
            }

            ValidationResult validation = validateBalance(event, conversion.amount());

            if (!validation.isApproved() && validation.sendToDlq()) {
                dlqProducer.send(event, validation.reason());
            }

            sendResult(event.transactionId(), validation.isApproved(), validation.reason(),
                    conversion.amount(), conversion.rate());

        } catch (Exception e) {
            log.error("Erro fatal processando transação {}", event.transactionId(), e);
            String reason = "INTERNAL_ERROR: " + e.getMessage();
            dlqProducer.send(event, reason);
            sendResult(event.transactionId(), false, reason, null, null);
        }
    }

    private ValidationResult validateBalance(TransactionCreatedEvent event, BigDecimal amount) {
        boolean isExpense = "EXPENSE".equalsIgnoreCase(event.type());
        boolean isAccount = TransactionOrigin.ACCOUNT.equals(event.origin());

        if (!isExpense) {
            log.info("Transação de receita (INCOME) aprovada automaticamente.");
            return ValidationResult.approve();
        }

        if (!isAccount) {
            log.info("Transação em dinheiro (CASH) aprovada automaticamente.");
            return ValidationResult.approve();
        }

        log.info("Despesa de conta (ACCOUNT). Consultando saldo externo...");
        return checkExternalBalance(event.userId(), amount);
    }

    private ValidationResult checkExternalBalance(Long userId, BigDecimal requiredAmount) {
        try {
            List<ExternalBalanceResponse> balances = getExternalBalanceSafe(userId);

            if (balances.isEmpty()) {
                log.info("Usuário {} sem carteira externa vinculada. Aprovando.", userId);
                return ValidationResult.approve();
            }

            BigDecimal totalBalance = balances.stream()
                    .map(ExternalBalanceResponse::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Saldo externo do usuário {}: {}", userId, totalBalance);

            if (totalBalance.compareTo(requiredAmount) >= 0) {
                return ValidationResult.approve();
            }

            log.warn("Saldo insuficiente. Necessário: {}, Disponível: {}", requiredAmount, totalBalance);
            return ValidationResult.reject("INSUFFICIENT_FUNDS", false);

        } catch (Exception e) {
            log.error("Erro ao consultar saldo externo para userId={}: {}", userId, e.getMessage(), e);
            return ValidationResult.reject("EXTERNAL_BANK_ERROR: " + e.getMessage(), true);
        }
    }

    private List<ExternalBalanceResponse> getExternalBalanceSafe(Long userId) {
        try {
            return mockApiClient.getBalance(userId);
        } catch (Exception e) {
            log.debug("Erro ao consultar saldo externo para usuário {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private void sendResult(Long transactionId, boolean approved, String reason,
                            BigDecimal convertedAmount, BigDecimal exchangeRate) {
        kafkaTemplate.send("transaction-processed", new TransactionProcessedEvent(
                transactionId, approved, reason, convertedAmount, exchangeRate
        ));
    }

    private record ValidationResult(boolean isApproved, String reason, boolean sendToDlq) {
        static ValidationResult approve() {
            return new ValidationResult(true, null, false);
        }

        static ValidationResult reject(String reason, boolean sendToDlq) {
            return new ValidationResult(false, reason, sendToDlq);
        }
    }
}
