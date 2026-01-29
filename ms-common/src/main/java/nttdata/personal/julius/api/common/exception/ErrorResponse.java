package nttdata.personal.julius.api.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String status;
    private String transactionId; // Mantendo o nome solicitado pelo usuário para ms-transaction
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;
}
