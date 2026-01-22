package nttdata.personal.julius.api.infrastructure.persistence.user;

import nttdata.personal.julius.api.domain.user.Cpf;
import nttdata.personal.julius.api.domain.user.Email;
import nttdata.personal.julius.api.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    @DisplayName("Deve converter Entidade para Domínio corretamente")
    void shouldConvertToDomain() {
        UserEntity entity = new UserEntity(
                UUID.randomUUID(),
                "Julius Rock",
                "julius@example.com",
                "10344573000",
                "hashed_password",
                LocalDateTime.now(),
                true
        );

        User domain = UserMapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getName()).isEqualTo(entity.getName());
        assertThat(domain.getEmail().email()).isEqualTo(entity.getEmail());
        assertThat(domain.getCpf().value()).isEqualTo(entity.getCpf());
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve converter Domínio para Entidade corretamente")
    void shouldConvertToEntity() {
        User domain = new User(
                UUID.randomUUID(),
                "Julius Rock",
                new Email("julius@example.com"),
                new Cpf("10344573000"),
                "hashed_password",
                LocalDateTime.now(),
                true
        );

        UserEntity entity = UserMapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getName()).isEqualTo(domain.getName());
        assertThat(entity.getEmail()).isEqualTo(domain.getEmail().email());
        assertThat(entity.getCpf()).isEqualTo(domain.getCpf().value());
    }
}
