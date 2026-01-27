package nttdata.personal.julius.api.application.port;

import nttdata.personal.julius.api.application.dto.TransactionCreatedEventDto;

/**
 * Port interface for publishing transaction events.
 * This abstraction allows the application layer to publish events
 * without depending on infrastructure details.
 */
public interface TransactionEventPort {

    /**
     * Publishes an event when a new transaction is created.
     *
     * @param event the transaction created event data
     */
    void publishTransactionCreated(TransactionCreatedEventDto event);
}
