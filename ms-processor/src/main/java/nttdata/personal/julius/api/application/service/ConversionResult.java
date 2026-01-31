package nttdata.personal.julius.api.application.service;

import java.math.BigDecimal;

public record ConversionResult(
        boolean success,
        BigDecimal amount,
        BigDecimal rate,
        String reason
) {
    public static ConversionResult success(BigDecimal amount, BigDecimal rate) {
        return new ConversionResult(true, amount, rate, null);
    }

    public static ConversionResult failure(String reason) {
        return new ConversionResult(false, null, null, reason);
    }

    public static ConversionResult noop(BigDecimal amount) {
        return new ConversionResult(true, amount, BigDecimal.ONE, null);
    }
}
