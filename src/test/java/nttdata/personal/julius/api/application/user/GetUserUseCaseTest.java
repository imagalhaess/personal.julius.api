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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserUseCase getUserUseCase;

    @Test
    @DisplayName("Deve retornar um usuário quando o ID existir")
    void shouldReturnUserWhenIdExists() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        User user = new User("Julius", new Email("julius@email.com"), new Cpf("12345678900"), "senha");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // ACT
        UserResponse response = getUserUseCase.execute(id);

        // ASSERT
        assertNotNull(response);
        assertEquals("Julius", response.name());
        assertEquals("julius@email.com", response.email());
    }
}
