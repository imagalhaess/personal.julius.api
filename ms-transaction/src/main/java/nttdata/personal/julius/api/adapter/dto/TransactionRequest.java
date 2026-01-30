package nttdata.personal.julius.api.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;

@Schema(description = "Dados para criação de transação")
public record TransactionRequest(
        @Schema(description = "ID do usuário (opcional, será pego do token se nulo)")
        Long userId,
        
        @Schema(description = "Valor da transação", example = "100.00")
        @NotNull @Positive BigDecimal amount,
        
        @Schema(description = "Moeda (padrão BRL)", example = "USD")
        String currency,
        
        @Schema(description = "Categoria")
        @NotNull Transaction.Category category,
        
        @Schema(description = "Tipo (INCOME/EXPENSE)")
        @NotNull Transaction.TransactionType type,

        @Schema(description = "Origem (ACCOUNT/CASH)", example = "ACCOUNT")
        @NotNull(message = "A origem é obrigatória (ACCOUNT ou CASH)")
        nttdata.personal.julius.api.common.domain.TransactionOrigin origin,
        
        @Schema(description = "Descrição", example = "Compra Internacional")
        String description
) {
    public TransactionRequest withUserId(Long userId) {
        return new TransactionRequest(userId, amount, currency, category, type, origin, description);
    }
}
