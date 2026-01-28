package nttdata.personal.julius.api.application.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nttdata.personal.julius.api.infrastructure.client.MockApiClient;
import nttdata.personal.julius.api.infrastructure.client.dto.ExternalBalanceResponse;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionCreatedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionProcessedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionResultProducer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessorService {

    private final TransactionResultProducer transactionResultProducer;
    private final MockApiClient mockApiClient;

    public void process(TransactionCreatedEvent event) {
        log.info("Processando transação: id={}, valor={}",
                event.transactionId(), event.amount());
        try {
            validateExternalBalance(event.userId(), event.amount());

            transactionResultProducer.send(new TransactionProcessedEvent(event.transactionId(), true, null));
            log.info("Transação {} APROVADA!", event.transactionId());

        } catch (IllegalArgumentException e) {
            // Rejeição de negócio (Saldo insuficiente ou Usuário não encontrado)
            String reason = e.getMessage();
            transactionResultProducer.send(new TransactionProcessedEvent(event.transactionId(), false, reason));
            log.warn("Transação {} REJEITADA: {}", event.transactionId(), reason);

        } catch (Exception e) {
            // Erro técnico imprevisto
            log.error("Erro técnico ao processar transação {}", event.transactionId(), e);
            try {
                transactionResultProducer.send(new TransactionProcessedEvent(event.transactionId(), false, "Erro técnico no processamento"));
            } catch (Exception ex) {
                log.error("CRITICAL: Falha ao enviar evento de rejeição para Kafka. Relançando erro para trigger de retry.", ex);
                throw new RuntimeException("Falha crítica na comunicação com Kafka", ex);
            }
        }
    }

    private void validateExternalBalance(Long userId, BigDecimal transactionAmount) {
        try {
            List<ExternalBalanceResponse> balances = mockApiClient.getBalance(userId);

            if (balances == null || balances.isEmpty()) {
                log.warn("Usuário {} não encontrado no banco externo (MockAPI) - Lista vazia.", userId);
                throw new IllegalArgumentException("Usuário não encontrado na instituição financeira.");
            }

            BigDecimal currentBalance = balances.get(0).amount();
            log.info("Saldo Externo: {} | Valor Transação: {}", currentBalance, transactionAmount);

            if (currentBalance.compareTo(transactionAmount) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente na conta externa.");
            }

        } catch (FeignException.NotFound e) {
            log.warn("Usuário {} não encontrado no banco externo (MockAPI) - 404.", userId);
            throw new IllegalArgumentException("Usuário não encontrado na instituição financeira.");
        } catch (Exception e) {
            // Se já for nossa exceção de negócio, apenas relança
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            log.error("Erro ao chamar MockAPI", e);
            throw new RuntimeException("Erro de comunicação com banco externo", e);
        }
    }
}
