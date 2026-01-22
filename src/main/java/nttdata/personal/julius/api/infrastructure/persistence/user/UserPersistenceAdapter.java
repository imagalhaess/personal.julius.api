package nttdata.personal.julius.api.infrastructure.persistence.user;

import nttdata.personal.julius.api.domain.user.Cpf;
import nttdata.personal.julius.api.domain.user.Email;
import nttdata.personal.julius.api.domain.user.User;
import nttdata.personal.julius.api.domain.user.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository; // Tem findByEmail e findByCpf

    public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.email()).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByCpf(Cpf cpf) {
        return jpaRepository.findByCpf(cpf.value()).map(UserMapper::toDomain);
    }

    @Override
    public void save(User user) {
        jpaRepository.save(UserMapper.toEntity(user));
    }
}
