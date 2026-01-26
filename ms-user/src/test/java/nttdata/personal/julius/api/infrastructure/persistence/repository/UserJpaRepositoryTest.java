package nttdata.personal.julius.api.infrastructure.persistence.repository;

import nttdata.personal.julius.api.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository repository;

    @Test
    @DisplayName("Deve encontrar usuário por e-mail")
    void shouldFindUserByEmail() {
        UserEntity entity = new UserEntity();
        entity.setName("Test");
        entity.setEmail("find@email.com");
        entity.setCpf("123");
        entity.setPassword("pass");
        
        repository.save(entity);

        var found = repository.findByEmail("find@email.com");
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test");
    }
}
