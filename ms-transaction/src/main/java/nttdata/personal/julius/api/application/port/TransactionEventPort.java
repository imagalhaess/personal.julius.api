package nttdata.personal.julius.api.application.port;

import nttdata.personal.julius.api.application.dto.TransactionCreatedEventDto;

public interface TransactionEventPort {

    void publishTransactionCreated(TransactionCreatedEventDto event);
}
