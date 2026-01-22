package nttdata.personal.julius.api.domain.user;

import nttdata.personal.julius.api.domain.BusinessException;

public record Cpf(String value) {

    public Cpf {
        if (value == null || value.isBlank()) {
            throw new BusinessException("CPF não pode ser nulo ou vazio");
        }

        value = value.replaceAll("\\D", "");

        if (value.length() != 11) {
            throw new BusinessException("CPF deve ter exatamente 11 dígitos");
        }

        if (value.matches("(\\d)\\1{10}")) {
            throw new BusinessException("CPF inválido");
        }

    }

    @Override
    public String toString() {
        return value;
    }

}