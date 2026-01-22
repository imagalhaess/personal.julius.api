package nttdata.personal.julius.api.infrastructure.persistence.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository repository;

    @Test
    @DisplayName("Deve salvar e buscar um usuário por ID com sucesso")
    void shouldSaveAndFindUserById() {
        UserEntity user = createValidUserEntity();
        UserEntity savedUser = repository.save(user);
        Optional<UserEntity> foundUser = repository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Julius Rock");
    }

    @Test
    @DisplayName("Não deve permitir salvar dois usuários com o mesmo e-mail")
    void shouldNotAllowDuplicateEmail() {
        UserEntity user1 = createValidUserEntity();
        repository.save(user1);

        UserEntity user2 = new UserEntity(
                UUID.randomUUID(),
                "Outro Nome",
                "julius@example.com",
                "810.479.100-12",
                "password",
                LocalDateTime.now(),
                true
        );

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(user2);
        });
    }

    private UserEntity createValidUserEntity() {
        return new UserEntity(
                UUID.randomUUID(),
                "Julius Rock",
                "julius@example.com",
                "810.479.100-12",
                "password123",
                LocalDateTime.now(),
                true
        );
    }
}