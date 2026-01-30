package nttdata.personal.julius.api.api.domain.model;

import nttdata.personal.julius.api.common.exception.DomainValidationException;
import nttdata.personal.julius.api.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Deve criar usuário corretamente")
    void shouldCreateUser() {
        User user = new User("Nome", "email@test.com", "123.456.789-00", "pass");

        assertNotNull(user);
        assertTrue(user.isActive());
        assertEquals("Nome", user.getName());
    }

    @Test
    @DisplayName("Deve desativar usuário")
    void shouldDeactivateUser() {
        User user = new User("Nome", "email@test.com", "123.456.789-00", "pass");

        user.deactivate();

        assertFalse(user.isActive());
    }

    @Test
    @DisplayName("Deve lançar exceção para nome vazio")
    void shouldThrowExceptionForEmptyName() {
        assertThrows(DomainValidationException.class, () ->
                new User("", "email@test.com", "123.456.789-00", "pass"));
    }

    @Test
    @DisplayName("Deve lançar exceção para email inválido")
    void shouldThrowExceptionForInvalidEmail() {
        assertThrows(DomainValidationException.class, () ->
                new User("Nome", "invalid-email", "123.456.789-00", "pass"));
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF inválido")
    void shouldThrowExceptionForInvalidCpf() {
        assertThrows(DomainValidationException.class, () ->
                new User("Nome", "email@test.com", "12345678900", "pass"));
    }
}
