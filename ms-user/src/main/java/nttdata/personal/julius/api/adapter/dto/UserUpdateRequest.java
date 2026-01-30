package nttdata.personal.julius.api.adapter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String name,

        @Email(message = "Formato de e-mail inválido")
        String email,

        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00")
        String cpf,

        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "A senha deve ser forte: pelo menos 8 caracteres, uma letra maiúscula, uma minúscula, um número e um caractere especial"
        )
        String password
) {}
