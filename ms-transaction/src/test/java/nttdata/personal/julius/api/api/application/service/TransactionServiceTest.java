package nttdata.personal.julius.api.api.application.service;

import nttdata.personal.julius.api.adapter.dto.BalanceResponse;
import nttdata.personal.julius.api.application.port.TransactionEventPort;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import nttdata.personal.julius.api.application.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionEventPort eventPort;

    @InjectMocks
    private TransactionService service;

    @Test
    @DisplayName("Deve calcular saldo (Receita - Despesa)")
    void shouldCalculateBalance() {
        Long userId = 1L;
        when(repository.sumIncomeByUserId(userId)).thenReturn(new BigDecimal("100"));
        when(repository.sumExpenseByUserId(userId)).thenReturn(new BigDecimal("40"));

        BalanceResponse balance = service.getBalance(userId);

        assertEquals(new BigDecimal("60"), balance.balance());
    }
}