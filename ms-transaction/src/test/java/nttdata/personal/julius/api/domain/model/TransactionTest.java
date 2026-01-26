package nttdata.personal.julius.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionTest {

    @Test
    @DisplayName("Deve criar transação com valores corretos")
    void shouldCreateTransaction() {
        Transaction t = new Transaction();
        t.setUserId(UUID.randomUUID());
        t.setAmount(BigDecimal.TEN);
        t.setType(Transaction.TransactionType.INCOME);

        assertNotNull(t);
        assertEquals(BigDecimal.TEN, t.getAmount());
    }
}
