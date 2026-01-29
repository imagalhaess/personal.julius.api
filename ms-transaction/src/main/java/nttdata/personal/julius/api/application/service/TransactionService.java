package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.adapter.dto.BalanceResponse;
import nttdata.personal.julius.api.adapter.dto.TransactionRequest;
import nttdata.personal.julius.api.adapter.dto.TransactionResponse;
import nttdata.personal.julius.api.application.port.TransactionEventPort;
import nttdata.personal.julius.api.common.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
        String currency = request.currency() != null ? request.currency().toUpperCase() : defaultCurrency;
        
        Transaction t = new Transaction();
        t.setUserId(request.userId());
        t.setAmount(request.amount());
        t.setCurrency(currency);
        t.setConvertedAmount(request.amount()); // Inicialmente igual ao amount se for BRL
        t.setExchangeRate(BigDecimal.ONE);
        t.setCategory(request.category());
        t.setType(request.type());
        t.setOrigin(request.origin() != null ? request.origin() : nttdata.personal.julius.api.common.domain.TransactionOrigin.ACCOUNT);
        t.setDescription(request.description());
        t.setCreatedAt(LocalDateTime.now());
        t.setStatus(Transaction.TransactionStatus.PENDING);

        Transaction saved = repository.save(t);

        // Publica evento
        eventPort.publishTransactionCreated(new nttdata.personal.julius.api.application.dto.TransactionCreatedEventDto(
                saved.getPublicId(), 
                saved.getUserId(), 
                saved.getAmount(), 
                saved.getCurrency(),
                saved.getType().name(), 
                saved.getCategory().name(),
                saved.getOrigin()
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

        return new BalanceResponse(
                income != null ? income : BigDecimal.ZERO,
                expense != null ? expense : BigDecimal.ZERO,
                (income != null ? income : BigDecimal.ZERO).subtract(expense != null ? expense : BigDecimal.ZERO)
        );
    }

    public void delete(Long transactionId, Long userId) {
        Transaction t = repository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada.", "NOT_FOUND"));
        repository.delete(t);
    }

    @Transactional
    public void approve(Long transactionId) {
        Transaction t = repository.findById(transactionId).orElseThrow();
        t.approve();
        repository.save(t);
    }

    @Transactional
    public void reject(Long transactionId, String reason) {
        Transaction t = repository.findById(transactionId).orElseThrow();
        t.reject();
        repository.save(t);
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest dto) {
        Transaction t = repository.findByIdAndUserId(id, dto.userId()).orElseThrow();
        t.setAmount(dto.amount());
        t.setCategory(dto.category());
        t.setDescription(dto.description());
        return toResponse(repository.save(t));
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getCurrency(),
                "BRL",
                t.getAmount(),
                t.getExchangeRate(),
                t.getConvertedAmount(),
                t.getStatus().name(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getCategory(),
                t.getType()
        );
    }
}
