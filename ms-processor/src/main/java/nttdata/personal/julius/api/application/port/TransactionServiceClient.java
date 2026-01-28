package nttdata.personal.julius.api.application.port;

public interface TransactionServiceClient {

    void approveTransaction(Long transactionId);

    void rejectTransaction(Long transactionId, String reason);
}
