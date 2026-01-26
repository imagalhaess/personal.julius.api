package nttdata.personal.julius.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Deve criar usuário corretamente")
    void shouldCreateUser() {
        User user = new User("Nome", "email@test.com", "123", "pass");
        
        assertNotNull(user);
        assertTrue(user.isActive()); // Default deve ser true
        assertEquals("Nome", user.getName());
    }

    @Test
    @DisplayName("Deve desativar usuário")
    void shouldDeactivateUser() {
        User user = new User("Nome", "email@test.com", "123", "pass");
        
        user.deactivate();
        
        assertFalse(user.isActive());
    }
}
