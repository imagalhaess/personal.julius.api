package nttdata.personal.julius.api.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados de resposta da transação")
public record TransactionResponse(
        @Schema(description = "ID da transação")
        Long id,
        
        @Schema(description = "Moeda de origem", example = "USD")
        String sourceCurrency,
        
        @Schema(description = "Moeda de destino", example = "BRL")
        String targetCurrency,
        
        @Schema(description = "Valor original na moeda de origem", example = "100.00")
        BigDecimal originalAmount,
        
        @Schema(description = "Taxa de câmbio aplicada", example = "5.25")
        BigDecimal exchangeRate,
        
        @Schema(description = "Valor convertido na moeda de destino", example = "525.00")
        BigDecimal convertedAmount,
        
        @Schema(description = "Status da transação", example = "APPROVED")
        String status,
        
        @Schema(description = "Descrição")
        String description,
        
        @Schema(description = "Data de criação")
        LocalDateTime createdAt,
        
        @Schema(description = "Categoria")
        Transaction.Category category,
        
        @Schema(description = "Tipo")
        Transaction.TransactionType type
) {}
