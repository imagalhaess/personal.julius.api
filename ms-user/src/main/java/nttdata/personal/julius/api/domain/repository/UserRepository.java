package nttdata.personal.julius.api.domain.repository;

import nttdata.personal.julius.api.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByPublicId(java.util.UUID publicId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    void delete(User user);

    List<User> findAll();

    List<User> findAllActive(int page, int size);
}