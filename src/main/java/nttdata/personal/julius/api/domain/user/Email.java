package nttdata.personal.julius.api.domain.user;

import nttdata.personal.julius.api.domain.BusinessException;

public record Email(String email) {

    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    public Email {
        if (email == null || email.isBlank()) {
            throw new BusinessException("E-mail não pode ser vazio");
        }

        email = email.trim().toLowerCase();

        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException("Formato de e-mail inválido");
        }
    }

    @Override
    public String toString() {
        return email;
    }

}