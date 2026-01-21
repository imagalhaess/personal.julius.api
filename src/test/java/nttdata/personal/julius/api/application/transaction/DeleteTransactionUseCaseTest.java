package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.domain.transaction.Transaction;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;
import nttdata.personal.julius.api.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DeleteTransactionUseCase deleteTransactionUseCase;

    @Test
    @DisplayName("Deve deletar transação com sucesso")
    void shouldDeleteTransactionSuccessfully() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        Transaction t = mock(Transaction.class);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(t));

        // ACT
        deleteTransactionUseCase.execute(id);

        // ASSERT
        verify(transactionRepository, times(1)).deleteById(id);
    }
}
