package nttdata.personal.julius.api.domain.transaction;

import nttdata.personal.julius.api.domain.BusinessException;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount == null) {
            throw new BusinessException("Valor não pode ser nulo");
        }
        if (currency == null) currency = "BRL";
    }
}

