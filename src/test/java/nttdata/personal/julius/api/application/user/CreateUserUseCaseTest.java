package nttdata.personal.julius.api.application.user;

import nttdata.personal.julius.api.application.user.dto.UserRequest;
import nttdata.personal.julius.api.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @Test
    void shouldCreateUserSuccessfully() {
        // ARRANGE
        var request = new UserRequest("Julius", "julius@email.com", "12345678900", "123456");

        // ACT
        var response = createUserUseCase.execute(request);

        // ASSERT
        assertNotNull(response);
        verify(userRepository, times(1)).save(any());
    }
}

