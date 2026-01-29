package nttdata.personal.julius.api.model;

import nttdata.personal.julius.api.common.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    @DisplayName("Deve criar transação com valores corretos")
    void shouldCreateTransaction() {
        Transaction t = new Transaction();
        t.setUserId(1L);
        t.setAmount(BigDecimal.TEN);
        t.setType(Transaction.TransactionType.INCOME);

        assertNotNull(t);
        assertEquals(BigDecimal.TEN, t.getAmount());
        assertEquals(Transaction.TransactionStatus.PENDING, t.getStatus());
    }

    @Test
    @DisplayName("Deve aprovar transação pendente")
    void shouldApproveTransaction() {
        Transaction t = new Transaction();
        t.setUserId(1L);
        t.setAmount(BigDecimal.TEN);

        t.approve();

        assertEquals(Transaction.TransactionStatus.APPROVED, t.getStatus());
    }

    @Test
    @DisplayName("Deve rejeitar transação pendente")
    void shouldRejectTransaction() {
        Transaction t = new Transaction();
        t.setUserId(1L);
        t.setAmount(BigDecimal.TEN);

        t.reject();

        assertEquals(Transaction.TransactionStatus.REJECTED, t.getStatus());
    }

    @Test
    @DisplayName("Não deve aprovar transação já aprovada")
    void shouldNotApproveAlreadyApprovedTransaction() {
        Transaction t = new Transaction();
        t.approve();

        assertThrows(BusinessException.class, t::approve);
    }

    @Test
    @DisplayName("Não deve rejeitar transação já rejeitada")
    void shouldNotRejectAlreadyRejectedTransaction() {
        Transaction t = new Transaction();
        t.reject();

        assertThrows(BusinessException.class, t::reject);
    }
}
