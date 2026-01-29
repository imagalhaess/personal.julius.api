package nttdata.personal.julius.api.adapter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        String name,

        @Email(message = "Formato de e-mail inválido")
        String email,

        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00")
        String cpf
) {}
