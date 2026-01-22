package nttdata.personal.julius.api.infrastructure.persistence.transaction;

import nttdata.personal.julius.api.domain.transaction.Money;
import nttdata.personal.julius.api.domain.transaction.Transaction;

public class TransactionMapper {
    public static TransactionEntity toEntity(Transaction domain) {
        return new TransactionEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getMoney().amount(),
                domain.getMoney().currency(),
                domain.getCategory(),
                domain.getType(),
                domain.getDescription(),
                domain.getTransactionDate(),
                domain.getCreatedAt()
        );
    }

    public static Transaction toDomain(TransactionEntity entity) {
        Money money = new Money(entity.getAmount(), entity.getCurrency());

        return new Transaction(
                entity.getId(),
                entity.getUserId(),
                money,
                entity.getCategory(),
                entity.getType(),
                entity.getDescription(),
                entity.getTransactionDate(),
                entity.getCreatedAt()
        );
    }
}

