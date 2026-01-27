package nttdata.personal.julius.api.domain.repository;

import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction domain operations.
 * Provides methods for CRUD operations and queries on Transaction entities.
 */
public interface TransactionRepository {

    /**
     * Saves a transaction to the repository.
     *
     * @param transaction the transaction to save
     * @return the saved transaction with generated ID
     */
    Transaction save(Transaction transaction);

    /**
     * Finds a transaction by its ID.
     *
     * @param id the transaction ID
     * @return an Optional containing the transaction if found
     */
    Optional<Transaction> findById(Long id);

    /**
     * Deletes a transaction from the repository.
     *
     * @param transaction the transaction to delete
     */
    void delete(Transaction transaction);

    /**
     * Finds a transaction by ID and user ID.
     *
     * @param id the transaction ID
     * @param userId the user ID
     * @return an Optional containing the transaction if found and belongs to the user
     */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds transactions by user ID with pagination.
     *
     * @param userId the user ID
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return a list of transactions for the requested page
     */
    List<Transaction> findByUserId(Long userId, int page, int size);

    /**
     * Sums all income transactions for a user.
     *
     * @param userId the user ID
     * @return the total income amount
     */
    BigDecimal sumIncomeByUserId(Long userId);

    /**
     * Sums all expense transactions for a user.
     *
     * @param userId the user ID
     * @return the total expense amount
     */
    BigDecimal sumExpenseByUserId(Long userId);
}
