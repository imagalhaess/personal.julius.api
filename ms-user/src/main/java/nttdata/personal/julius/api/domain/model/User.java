package nttdata.personal.julius.api.domain.model;

import nttdata.personal.julius.api.common.exception.DomainValidationException;

public class User {
    private Long id;
    private String name;
    private String email;
    private String cpf;
    private String password;
    private boolean active;

    public User(Long id, String name, String email, String cpf, String password, boolean active) {
        validateName(name);
        validateEmail(email);
        validateCpf(cpf);

        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.active = active;
    }

    public User(String name, String email, String cpf, String password) {
        validateName(name);
        validateEmail(email);
        validateCpf(cpf);

        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.active = true;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Nome é obrigatório");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
            throw new DomainValidationException("E-mail inválido");
        }
    }

    private void validateCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            throw new DomainValidationException("CPF inválido. Formato esperado: 000.000.000-00");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
