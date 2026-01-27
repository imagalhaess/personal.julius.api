package nttdata.personal.julius.api.infrastructure.persistence.repository;

import nttdata.personal.julius.api.domain.model.User;
import nttdata.personal.julius.api.domain.repository.UserRepository;
import nttdata.personal.julius.api.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }

    @Override
    public void delete(User user) {
        jpaRepository.deleteById(user.getId());
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll()
                .stream().map(UserEntity::toDomain)
                .toList();
    }

    @Override
    public List<User> findAllActive(int page, int size) {
        return jpaRepository.findAllActive(PageRequest.of(page, size))
                .stream().map(UserEntity::toDomain)
                .toList();
    }
}
