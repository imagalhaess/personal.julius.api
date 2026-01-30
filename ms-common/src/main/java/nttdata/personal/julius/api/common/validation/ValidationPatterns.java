package nttdata.personal.julius.api.common.validation;

public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    // CPF
    public static final String CPF_PATTERN = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}";
    public static final String CPF_MESSAGE = "O CPF deve seguir o formato 000.000.000-00";

    // Email
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String EMAIL_MESSAGE = "O e-mail deve ser valido";

    // Senha forte
    public static final String STRONG_PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    public static final String STRONG_PASSWORD_MESSAGE = "A senha deve ter no minimo 8 caracteres, incluindo maiuscula, minuscula, numero e caractere especial";

    // Limites de transacao
    public static final String TRANSACTION_LIMIT_EXCEEDED = "LIMIT_EXCEEDED";
    public static final java.math.BigDecimal MAX_ACCOUNT_TRANSACTION = new java.math.BigDecimal("10000.00");
}
