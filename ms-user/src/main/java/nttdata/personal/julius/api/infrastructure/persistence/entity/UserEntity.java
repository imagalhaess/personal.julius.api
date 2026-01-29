package nttdata.personal.julius.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID publicId;

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

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

    public nttdata.personal.julius.api.domain.model.User toDomain() {
        return new nttdata.personal.julius.api.domain.model.User(
                this.id,
                this.publicId,
                this.name,
                this.email,
                this.cpf,
                this.password,
                this.active
        );
    }

    public static UserEntity fromDomain(nttdata.personal.julius.api.domain.model.User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setPublicId(user.getPublicId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setCpf(user.getCpf());
        entity.setPassword(user.getPassword());
        entity.setActive(user.isActive());
        return entity;
    }
}
