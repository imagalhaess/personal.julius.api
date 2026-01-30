package nttdata.personal.julius.api.api.application.service;

import nttdata.personal.julius.api.adapter.dto.UserRequest;
import nttdata.personal.julius.api.application.service.UserService;
import nttdata.personal.julius.api.common.exception.BusinessException;
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
        UserRequest request = new UserRequest("User", "duplicate@email.com", "123", "pass");

        when(repository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.create(request));
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