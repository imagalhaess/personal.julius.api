package nttdata.personal.julius.api.domain.user;

import nttdata.personal.julius.api.domain.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("Deve criar Email válido corretamente")
    void shouldCreateEmailWhenValid() {
        var input = " Usuario@Domain.com ";

        var email = new Email(input);

        assertEquals("usuario@domain.com", email.email());
    }

    @Test
    @DisplayName("Deve lançar erro para email nulo ou vazio")
    void shouldThrowExceptionWhenNullOrEmpty() {
        assertThrows(BusinessException.class, () -> new Email(null));
        assertThrows(BusinessException.class, () -> new Email("   "));
    }

    @Test
    @DisplayName("Deve lançar erro para formatos inválidos")
    void shouldThrowExceptionWhenFormatIsInvalid() {

        assertThrows(BusinessException.class, () -> new Email("usuario.com"));
        assertThrows(BusinessException.class, () -> new Email("usuario@"));
        assertThrows(BusinessException.class, () -> new Email("usuario@gmail"));
        assertThrows(BusinessException.class, () -> new Email("usuario@gmail.com."));
    }
}