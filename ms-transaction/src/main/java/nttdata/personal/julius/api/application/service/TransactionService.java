package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.adapter.dto.BalanceResponse;
import nttdata.personal.julius.api.adapter.dto.TransactionRequest;
import nttdata.personal.julius.api.adapter.dto.TransactionResponse;
import nttdata.personal.julius.api.application.dto.TransactionCreatedEventDto;
import nttdata.personal.julius.api.application.port.TransactionEventPort;
import nttdata.personal.julius.api.common.exception.BusinessException;
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
    public TransactionResponse create(TransactionRequest request) {
        Transaction t = new Transaction();
        t.setUserId(request.userId());
        t.setAmount(request.amount());
        String currency = request.currency() != null ? request.currency() : defaultCurrency;
        t.setCurrency(currency);
        t.setConvertedAmount(request.amount());
        t.setCategory(request.category());
        t.setType(request.type());
        t.setDescription(request.description());
        t.setCreatedAt(java.time.LocalDateTime.now());

        Transaction saved = repository.save(t);

        eventPort.publishTransactionCreated(new TransactionCreatedEventDto(
                saved.getId(), 
                saved.getUserId(), 
                saved.getAmount(), 
                saved.getCurrency(),
                saved.getType().name(), 
                saved.getCategory().name(),
                saved.getCreatedAt()
        ));

        return toResponse(saved);
    }

    public List<TransactionResponse> list(Long userId, int page, int size) {
        return repository.findByUserId(userId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BalanceResponse getBalance(Long userId) {
        BigDecimal income = repository.sumIncomeByUserId(userId);
        BigDecimal expense = repository.sumExpenseByUserId(userId);

        if (income == null) {
            income = BigDecimal.ZERO;
        }
        if (expense == null) {
            expense = BigDecimal.ZERO;
        }

        return new BalanceResponse(income, expense, income.subtract(expense));
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

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getAmount(), t.getStatus().name(),
                t.getDescription(), t.getCreatedAt(),
                t.getCategory(), t.getType()
        );
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest dto) {
        Transaction t = repository.findByIdAndUserId(id, dto.userId())
                .orElseThrow(() -> new BusinessException("Transação não encontrada ou acesso negado."));

        t.setAmount(dto.amount());
        t.setCurrency(dto.currency() != null ? dto.currency() : defaultCurrency);
        t.setCategory(dto.category());
        t.setType(dto.type());
        t.setDescription(dto.description());

        t.setStatus(Transaction.TransactionStatus.PENDING);

        Transaction updated = repository.save(t);

        return toResponse(updated);
    }
}
