package nttdata.personal.julius.api.domain.repository;

import nttdata.personal.julius.api.domain.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User domain operations.
 * Provides methods for CRUD operations and queries on User entities.
 */
public interface UserRepository {

    /**
     * Saves a user to the repository.
     *
     * @param user the user to save
     * @return the saved user with generated ID
     */
    User save(User user);

    /**
     * Finds a user by their ID.
     *
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by their email address.
     *
     * @param email the email address
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user with the given CPF exists.
     *
     * @param cpf the CPF to check
     * @return true if a user with this CPF exists
     */
    boolean existsByCpf(String cpf);

    /**
     * Deletes a user from the repository.
     *
     * @param user the user to delete
     */
    void delete(User user);

    /**
     * Finds all users.
     *
     * @return a list of all users
     */
    List<User> findAll();

    /**
     * Finds all active users with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return a list of active users for the requested page
     */
    List<User> findAllActive(int page, int size);
}