package nttdata.personal.julius.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nttdata.personal.julius.api.domain.model.User;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean active = true;

    // Métodos de conversão (Mappers)
    public static UserEntity fromDomain(User user) {
        return new UserEntity(user.getId(), user.getName(), user.getEmail(), user.getCpf(), user.getPassword(), user.isActive());
    }

    public User toDomain() {
        return new User(this.id, this.name, this.email, this.cpf, this.password, this.active);
    }
}
