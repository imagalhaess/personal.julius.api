package nttdata.personal.julius.api.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criacao de usuario")
public record UserRequest(
        @Schema(description = "Nome completo do usuario", example = "Joao Silva")
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @Schema(description = "E-mail do usuario", example = "joao@email.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Schema(description = "CPF no formato 000.000.000-00", example = "123.456.789-00")
        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00")
        String cpf,

        @Schema(description = "Senha forte com minimo 8 caracteres", example = "Senha@123")
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "A senha deve ser forte: pelo menos 8 caracteres, uma letra maiúscula, uma minúscula, um número e um caractere especial"
        )
        String password
) {}
