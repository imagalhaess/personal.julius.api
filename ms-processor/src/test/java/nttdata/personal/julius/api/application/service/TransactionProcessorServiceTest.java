package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.common.event.TransactionCreatedEvent;
import nttdata.personal.julius.api.common.event.TransactionProcessedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionProcessorServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private TransactionProcessorService service;

    @BeforeEach
    void setUp() {
        service = new TransactionProcessorService(kafkaTemplate);
    }

    @Test
    void shouldProcessAndApproveTransaction() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L, 1L, new BigDecimal("100.00"), "BRL", "EXPENSE", "FOOD", 
                nttdata.personal.julius.api.common.domain.TransactionOrigin.ACCOUNT, LocalDateTime.now()
        );

        service.process(event);

        ArgumentCaptor<TransactionProcessedEvent> captor = ArgumentCaptor.forClass(TransactionProcessedEvent.class);
        verify(kafkaTemplate).send(eq("transaction-processed"), captor.capture());
        
        TransactionProcessedEvent result = captor.getValue();
        assertEquals(1L, result.transactionId());
        assertTrue(result.approved());
        assertNull(result.reason());
    }

    @Test
    void shouldRejectTransactionWhenLimitExceeded() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L, 1L, new BigDecimal("15000.00"), "BRL", "EXPENSE", "FOOD", 
                nttdata.personal.julius.api.common.domain.TransactionOrigin.ACCOUNT, LocalDateTime.now()
        );

        service.process(event);

        ArgumentCaptor<TransactionProcessedEvent> captor = ArgumentCaptor.forClass(TransactionProcessedEvent.class);
        verify(kafkaTemplate).send(eq("transaction-processed"), captor.capture());
        
        TransactionProcessedEvent result = captor.getValue();
        assertEquals(1L, result.transactionId());
        assertFalse(result.approved());
        assertEquals("LIMIT_EXCEEDED", result.reason());
    }

    @Test
    void shouldApproveCashTransactionAutomatically() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L, 1L, new BigDecimal("20000.00"), "BRL", "EXPENSE", "FOOD", 
                nttdata.personal.julius.api.common.domain.TransactionOrigin.CASH, LocalDateTime.now()
        );

        service.process(event);

        ArgumentCaptor<TransactionProcessedEvent> captor = ArgumentCaptor.forClass(TransactionProcessedEvent.class);
        verify(kafkaTemplate).send(eq("transaction-processed"), captor.capture());
        
        TransactionProcessedEvent result = captor.getValue();
        assertEquals(1L, result.transactionId());
        assertTrue(result.approved());
        assertNull(result.reason());
    }
}
