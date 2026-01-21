package nttdata.personal.julius.api.application.user;

import nttdata.personal.julius.api.application.user.dto.UserResponse;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    @Test
    @DisplayName("Deve desativar um usuário (Soft Delete) com sucesso")
    void shouldDeactivateUserSuccessfully() {
        // Given
        UUID id = UUID.randomUUID();
        User user = new User("Julius", new Email("julius@email.com"), new Cpf("12345678900"), "senha");
        // O usuário nasce ativo (true)
        assertTrue(user.isActive());

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // When
        UserResponse response = deleteUserUseCase.execute(id);

        // Then
        assertFalse(user.isActive()); // A entidade foi desativada
        assertFalse(response.active()); // O DTO reflete a desativação
        verify(userRepository, times(1)).save(user); // O save foi chamado para persistir a mudança
    }
}
