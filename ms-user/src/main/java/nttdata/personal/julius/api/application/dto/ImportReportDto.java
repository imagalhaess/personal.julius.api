package nttdata.personal.julius.api.application.dto;

import java.util.List;

public record ImportReportDto(
        int totalSuccess,
        int totalFailure,
        List<ImportError> errors
) {
    public record ImportError(int row, String message) {
    }
}