package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.dto.BalanceResponseDto;
import nttdata.personal.julius.api.application.dto.TransactionCreatedEventDto;
import nttdata.personal.julius.api.application.dto.TransactionRequestDto;
import nttdata.personal.julius.api.application.dto.TransactionResponseDto;
import nttdata.personal.julius.api.application.port.TransactionEventPort;
import nttdata.personal.julius.common.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionEventPort eventPort;

    @Value("${app.default-currency:BRL}")
    private String defaultCurrency;

    public TransactionService(TransactionRepository repository, TransactionEventPort eventPort) {
        this.repository = repository;
        this.eventPort = eventPort;
    }

    @Transactional
    public TransactionResponseDto create(TransactionRequestDto request) {
        Transaction t = new Transaction();
        t.setUserId(request.userId());
        t.setAmount(request.amount());
        t.setCurrency(request.currency() != null ? request.currency() : defaultCurrency);
        t.setCategory(request.category());
        t.setType(request.type());
        t.setDescription(request.description());
        t.setTransactionDate(request.date());

        Transaction saved = repository.save(t);

        eventPort.publishTransactionCreated(new TransactionCreatedEventDto(
                saved.getId(), saved.getUserId(), saved.getAmount(), saved.getCurrency(),
                saved.getType().name(), saved.getCategory().name()
        ));

        return toResponseDto(saved);
    }

    public List<TransactionResponseDto> list(Long userId, int page, int size) {
        return repository.findByUserId(userId, page, size)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public BalanceResponseDto getBalance(Long userId) {
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

    public void delete(Long transactionId, Long userId) {
        Transaction t = repository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada ou acesso negado."));

        repository.delete(t);
    }

    @Transactional
    public void approve(Long transactionId) {
        Transaction t = repository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada: " + transactionId));

        t.approve();
        repository.save(t);
    }

    @Transactional
    public void reject(Long transactionId, String reason) {
        Transaction t = repository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada: " + transactionId));

        t.reject();
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
