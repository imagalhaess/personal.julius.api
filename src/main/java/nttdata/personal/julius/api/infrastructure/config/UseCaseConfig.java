package nttdata.personal.julius.api.infrastructure.config;

import nttdata.personal.julius.api.application.transaction.*;
import nttdata.personal.julius.api.application.user.CreateUserUseCase;
import nttdata.personal.julius.api.application.user.DeleteUserUseCase;
import nttdata.personal.julius.api.application.user.GetUserUseCase;
import nttdata.personal.julius.api.application.user.UpdateUserUseCase;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;
import nttdata.personal.julius.api.domain.user.UserRepository;
import nttdata.personal.julius.api.infrastructure.messaging.kafka.producer.TransactionEventProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository) {
        return new CreateUserUseCase(userRepository);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository) {
        return new UpdateUserUseCase(userRepository);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepository userRepository) {
        return new GetUserUseCase(userRepository);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUserUseCase(userRepository);
    }

    @Bean
    public CreateTransactionUseCase createTransactionUseCase(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            TransactionEventProducer eventProducer) {
        return new CreateTransactionUseCase(transactionRepository, userRepository, eventProducer);
    }

    @Bean
    public UpdateTransactionUseCase updateTransactionUseCase(
            TransactionRepository transactionRepository) {
        return new UpdateTransactionUseCase(transactionRepository);
    }

    @Bean
    public GetTransactionUseCase getTransactionUseCase(TransactionRepository transactionRepository) {
        return new GetTransactionUseCase(transactionRepository);
    }

    @Bean
    public DeleteTransactionUseCase deleteTransactionUseCase(TransactionRepository transactionRepository) {
        return new DeleteTransactionUseCase(transactionRepository);
    }

    @Bean
    public GetBalanceUseCase getBalanceUseCase(TransactionRepository transactionRepository) {
        return new GetBalanceUseCase(transactionRepository);
    }

    @Bean
    public ProcessTransactionUseCase processTransactionUseCase(TransactionRepository transactionRepository) {
        return new ProcessTransactionUseCase(transactionRepository);
    }
}