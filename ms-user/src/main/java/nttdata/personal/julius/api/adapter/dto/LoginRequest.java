package nttdata.personal.julius.api.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para autenticacao")
public record LoginRequest(
        @Schema(description = "E-mail do usuario", example = "joao@email.com")
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Schema(description = "Senha do usuario", example = "Senha@123")
        @NotBlank(message = "Senha é obrigatória")
        String password
) {}
