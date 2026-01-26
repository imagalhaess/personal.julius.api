package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.dto.BalanceResponseDto;
import nttdata.personal.julius.api.application.dto.TransactionRequestDto;
import nttdata.personal.julius.api.application.dto.TransactionResponseDto;
import nttdata.personal.julius.api.domain.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionCreatedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionEventProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionEventProducer producer;

    public TransactionService(TransactionRepository repository, TransactionEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Transactional
    public TransactionResponseDto create(TransactionRequestDto request) {
        Transaction t = new Transaction();
        t.setUserId(request.userId());
        t.setAmount(request.amount());
        t.setCurrency(request.currency() != null ? request.currency() : "BRL");
        t.setCategory(request.category());
        t.setType(request.type());
        t.setDescription(request.description());
        t.setTransactionDate(request.date());

        // Save using repository adapter (returns domain model)
        Transaction saved = repository.save(t);

        // Send event
        producer.send(new TransactionCreatedEvent(
                saved.getId(), saved.getUserId(), saved.getAmount(), saved.getCurrency(),
                saved.getType().name(), saved.getCategory().name()
        ));

        return toResponseDto(saved);
    }

    public List<TransactionResponseDto> list(UUID userId, int page, int size) {
        return repository.findByUserId(userId, page, size)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public BalanceResponseDto getBalance(UUID userId) {
        BigDecimal income = repository.sumIncomeByUserId(userId);
        BigDecimal expense = repository.sumExpenseByUserId(userId);

        if (income == null) {
            income = BigDecimal.ZERO;
        }
        if (expense == null) {
            expense = BigDecimal.ZERO;
        }

        return new BalanceResponseDto(income, expense, income.subtract(expense));
    }

    public void delete(UUID transactionId, UUID userId) {
        Transaction t = repository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada ou acesso negado."));

        repository.delete(t);
    }

    @Transactional
    public void approve(UUID transactionId) {
        Transaction t = repository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada: " + transactionId));

        t.setStatus(Transaction.TransactionStatus.APPROVED);
        repository.save(t);
    }

    @Transactional
    public void reject(UUID transactionId, String reason) {
        Transaction t = repository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada: " + transactionId));

        t.setStatus(Transaction.TransactionStatus.REJECTED);
        repository.save(t);
    }

    private TransactionResponseDto toResponseDto(Transaction t) {
        return new TransactionResponseDto(
                t.getId(), t.getAmount(), t.getStatus().name(),
                t.getDescription(), t.getTransactionDate(),
                t.getCategory(), t.getType()
        );
    }
}
