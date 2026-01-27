package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.dto.UserDto;
import nttdata.personal.julius.api.application.dto.UserUpdateDto;
import nttdata.personal.julius.common.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.User;
import nttdata.personal.julius.api.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve lançar exceção se e-mail já existir ao criar")
    void shouldThrowExceptionWhenEmailExists() {
        UserDto dto = new UserDto("User", "duplicate@email.com", "123", "pass");

        when(repository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.create(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve realizar soft delete")
    void shouldSoftDeleteUser() {
        Long id = 1L;
        User user = new User("User", "email@test.com", "123.456.789-00", "pass");
        user.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));

        userService.delete(id);

        assertFalse(user.isActive());
        verify(repository).save(user);
    }
}