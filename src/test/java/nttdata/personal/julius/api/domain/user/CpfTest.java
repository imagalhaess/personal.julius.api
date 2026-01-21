package nttdata.personal.julius.api.domain.user;

import nttdata.personal.julius.api.domain.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfTest {

    @Test
    @DisplayName("Deve criar CPF se tiver 11 dígitos e não for tudo igual (KISS)")
    void shouldCreateCpfWhenFormatIsValid() {
        var input = "123.456.789-00";

        var cpf = new Cpf(input);

        assertEquals("12345678900", cpf.value());
    }

    @Test
    @DisplayName("Deve lançar erro se CPF for nulo ou vazio")
    void shouldThrowExceptionWhenNullOrEmpty() {
        assertThrows(BusinessException.class, () -> new Cpf(null));
        assertThrows(BusinessException.class, () -> new Cpf(""));
    }

    @Test
    @DisplayName("Deve lançar erro se tamanho for diferente de 11 dígitos")
    void shouldThrowExceptionWhenLengthIsInvalid() {

        assertThrows(BusinessException.class, () -> new Cpf("123.456.789"));
        assertThrows(BusinessException.class, () -> new Cpf("1234567890123"));
    }

    @Test
    @DisplayName("Deve lançar erro se todos os dígitos forem iguais")
    void shouldThrowExceptionWhenAllDigitsAreEqual() {

        assertThrows(BusinessException.class, () -> new Cpf("111.111.111-11"));
    }
}