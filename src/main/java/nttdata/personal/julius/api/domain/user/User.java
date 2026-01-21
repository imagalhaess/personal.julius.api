package nttdata.personal.julius.api.domain.user;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

    private UUID id;
    private String name;
    private Email email;
    private Cpf cpf;
    private String passwordHash;
    private LocalDateTime createdAt;
    private boolean active;

    public User(String name, Email email, Cpf cpf, String passwordHash) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public void deactivate(){
        this.active = false;
    }
}
