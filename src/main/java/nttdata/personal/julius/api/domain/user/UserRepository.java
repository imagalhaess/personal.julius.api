package nttdata.personal.julius.api.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void save(User user);
    Optional<User> findById (UUID id);
    Optional<User> findByEmail (Email email);
    Optional<User> findByCpf (Cpf value);

}
