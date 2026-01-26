package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.dto.BalanceResponseDto;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionEventProducer producer;

    @InjectMocks
    private TransactionService service;

    @Test
    @DisplayName("Deve calcular saldo (Receita - Despesa)")
    void shouldCalculateBalance() {
        UUID userId = UUID.randomUUID();
        when(repository.sumIncomeByUserId(userId)).thenReturn(new BigDecimal("100"));
        when(repository.sumExpenseByUserId(userId)).thenReturn(new BigDecimal("40"));

        BalanceResponseDto balance = service.getBalance(userId);

        assertEquals(new BigDecimal("60"), balance.balance());
    }
}