package nttdata.personal.julius.api.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados de resposta do usuário")
public record UserResponse(
        @Schema(description = "ID público do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Nome do usuário", example = "João Silva")
        String name,
        
        @Schema(description = "E-mail do usuário", example = "joao@email.com")
        String email
) {}
