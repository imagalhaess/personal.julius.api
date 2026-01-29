package nttdata.personal.julius.api.application.service;

import feign.FeignException;
import nttdata.personal.julius.api.infrastructure.client.BrasilApiClient;
import nttdata.personal.julius.api.infrastructure.client.MockApiClient;
import nttdata.personal.julius.api.infrastructure.client.dto.ExternalBalanceResponse;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionCreatedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionProcessedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionResultProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionProcessorServiceTest {

    @Mock
    private TransactionResultProducer producer;

    @Mock
    private MockApiClient mockApiClient;

    @Mock
    private BrasilApiClient brasilApiClient;

    private TransactionProcessorService service;

    @BeforeEach
    void setUp() {
        service = new TransactionProcessorService(producer, mockApiClient, brasilApiClient);
    }

    @Test
    void shouldApprove_WhenBalanceIsSufficient() {
        BigDecimal amountBrl = new BigDecimal("50.00");
        BigDecimal balance = new BigDecimal("100.00");

        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L, 202L, amountBrl, "BRL", "EXPENSE", "FOOD", LocalDateTime.now()
        );

        when(mockApiClient.getBalance(202L))
                .thenReturn(List.of(new ExternalBalanceResponse("202", 202L, balance, "BRL")));

        service.process(event);

        ArgumentCaptor<TransactionProcessedEvent> captor = ArgumentCaptor.forClass(TransactionProcessedEvent.class);
        verify(producer).send(captor.capture());

        assertTrue(captor.getValue().approved());
    }

    @Test
    void shouldReject_WhenUserNotFoundInMockAPI() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L, 999L, BigDecimal.TEN, "BRL", "EXPENSE", "FOOD", LocalDateTime.now()
        );

        when(mockApiClient.getBalance(999L)).thenThrow(FeignException.NotFound.class);

        service.process(event);

        ArgumentCaptor<TransactionProcessedEvent> captor = ArgumentCaptor.forClass(TransactionProcessedEvent.class);
        verify(producer).send(captor.capture());

        assertFalse(captor.getValue().approved());
        assertEquals("Usuário não encontrado na instituição financeira.", captor.getValue().reason());
    }

    @Test
    void shouldApproveIncome_WithoutCheckingBalance() {
        // Given
        BigDecimal amountBrl = new BigDecimal("5000.00");
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L, 202L, amountBrl, "BRL", "INCOME", "SALARY", LocalDateTime.now()
        );

        // When
        service.process(event);

        // Then
        ArgumentCaptor<TransactionProcessedEvent> captor = ArgumentCaptor.forClass(TransactionProcessedEvent.class);
        verify(producer).send(captor.capture());
        verify(mockApiClient, never()).getBalance(anyLong());

        assertTrue(captor.getValue().approved());
    }

    @Test
    void shouldApproveExternal_WithoutCheckingBalance() {
        BigDecimal amountBrl = new BigDecimal("10.00");
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L, 202L, amountBrl, "BRL", "EXTERNAL", "FOOD", LocalDateTime.now()
        );

        service.process(event);

        ArgumentCaptor<TransactionProcessedEvent> captor = ArgumentCaptor.forClass(TransactionProcessedEvent.class);
        verify(producer).send(captor.capture());
        verify(mockApiClient, never()).getBalance(anyLong());

        assertTrue(captor.getValue().approved());
    }
}
