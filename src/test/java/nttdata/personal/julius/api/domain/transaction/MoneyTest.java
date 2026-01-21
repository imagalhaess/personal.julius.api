package nttdata.personal.julius.api.domain.transaction;

import nttdata.personal.julius.api.domain.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Deve criar Money com sucesso quando valor for válido")
    void shouldCreateMoneyWhenValid() {
        BigDecimal amount = new BigDecimal("100.50");
        Money money = new Money(amount, "BRL");

        assertEquals(amount, money.amount());
        assertEquals("BRL", money.currency());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor for nulo")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThrows(BusinessException.class, () -> new Money(null, "BRL"));
    }
}