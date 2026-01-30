package nttdata.personal.julius.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nttdata.personal.julius.api.common.exception.DomainValidationException;

import java.util.UUID;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private UUID publicId;
    private String name;
    private String email;
    private String cpf;
    private String password;
    private boolean active;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String CPF_REGEX = "^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$";

    // Construtor para criação de novo usuário (sem ID)
    public User(String name, String email, String cpf, String password) {
        validate(name, email, cpf);
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.active = true;
    }

    // Construtor para atualização (com ID mas sem publicId)
    public User(Long id, String name, String email, String cpf, String password, boolean active) {
        validate(name, email, cpf);
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.active = active;
    }

    private void validate(String name, String email, String cpf) {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainValidationException("Name cannot be empty");
        }
        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new DomainValidationException("Invalid email format");
        }
        if (cpf == null || !Pattern.matches(CPF_REGEX, cpf)) {
            throw new DomainValidationException("Invalid CPF format");
        }
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }
}
