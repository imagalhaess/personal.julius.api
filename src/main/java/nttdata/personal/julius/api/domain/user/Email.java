package nttdata.personal.julius.api.domain.user;

import nttdata.personal.julius.api.domain.BusinessException;

public record Email  (String email) {

    public Email {
        if (email == null || email.isBlank()) {
            throw new BusinessException("E-mail não pode ser vazio");
        }

        email = email.trim().toLowerCase();

        if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".")) {
            throw new BusinessException("E-mail com formato inválido");
        }
    }

    @Override
    public String toString() {
        return email;
    }

}
