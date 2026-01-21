package nttdata.personal.julius.api.application.user;

import nttdata.personal.julius.api.application.user.dto.UserRequest;
import nttdata.personal.julius.api.application.user.dto.UserResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.user.Cpf;
import nttdata.personal.julius.api.domain.user.Email;
import nttdata.personal.julius.api.domain.user.User;
import nttdata.personal.julius.api.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldUpdateUserSuccessfully() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        User existingUser = new User("Nome Antigo", new Email("antigo@email.com"), new Cpf("12345678900"), "senha");
        UserRequest request = new UserRequest("Nome Novo", "novo@email.com", null, null);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        // ACT
        UserResponse response = updateUserUseCase.execute(id, request);

        // ASSERT
        assertEquals("Nome Novo", response.name());
        assertEquals("novo@email.com", response.email());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID do usuário não for encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // ARRANGE
        UUID idInexistente = UUID.randomUUID();
        UserRequest request = new UserRequest("Qualquer Nome", "qualquer@email.com", null, null);

        when(userRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // ACT&ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            updateUserUseCase.execute(idInexistente, request);
        });

        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando e-mail já pertence a outro usuário")
    void shouldThrowExceptionWhenEmailAlreadyExistsForAnotherUser() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();

        User existingUser = new User("User Atual", new Email("atual@email.com"), new Cpf("12345678900"), "senha");
        User outroUser = new User("Outro User", new Email("novo@email.com"), new Cpf("98765432100"), "senha");

        UserRequest request = new UserRequest("Nome Novo", "novo@email.com", null, null);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(outroUser));

        // ACT&ASSERT
        assertThrows(BusinessException.class, () -> updateUserUseCase.execute(id, request));
    }
}
