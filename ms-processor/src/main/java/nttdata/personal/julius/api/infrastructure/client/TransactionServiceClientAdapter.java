package nttdata.personal.julius.api.infrastructure.client;

import nttdata.personal.julius.api.application.port.TransactionServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TransactionServiceClientAdapter implements TransactionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceClientAdapter.class);

    private final RestClient restClient;

    public TransactionServiceClientAdapter(
            @Value("${app.services.transaction.url:http://localhost:8082}") String transactionServiceUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(transactionServiceUrl)
                .build();
    }

    @Override
    public void approveTransaction(Long transactionId) {
        log.debug("Enviando aprovação para transação {}", transactionId);
        restClient.post()
                .uri("/transactions/{id}/approve", transactionId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void rejectTransaction(Long transactionId, String reason) {
        log.debug("Enviando rejeição para transação {}: {}", transactionId, reason);
        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/transactions/{id}/reject")
                        .queryParam("reason", reason)
                        .build(transactionId))
                .retrieve()
                .toBodilessEntity();
    }
}
