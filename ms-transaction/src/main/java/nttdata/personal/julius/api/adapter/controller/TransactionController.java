package nttdata.personal.julius.api.adapter.controller;

import jakarta.validation.Valid;
import nttdata.personal.julius.api.application.dto.BalanceResponseDto;
import nttdata.personal.julius.api.application.dto.TransactionRequestDto;
import nttdata.personal.julius.api.application.dto.TransactionResponseDto;
import nttdata.personal.julius.api.application.service.TransactionService;
import nttdata.personal.julius.api.adapter.dto.BalanceResponse;
import nttdata.personal.julius.api.adapter.dto.TransactionRequest;
import nttdata.personal.julius.api.adapter.dto.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestBody @Valid TransactionRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        TransactionRequestDto dto = new TransactionRequestDto(
                userId, request.amount(), request.currency(), request.category(),
                request.type(), request.description(), request.date()
        );

        TransactionResponseDto responseDto = service.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(responseDto));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        List<TransactionResponse> list = service.list(userId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        BalanceResponseDto dto = service.getBalance(userId);
        return ResponseEntity.ok(new BalanceResponse(dto.totalIncome(), dto.totalExpense(), dto.balance()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private TransactionResponse toResponse(TransactionResponseDto dto) {
        return new TransactionResponse(
                dto.id(), dto.amount(), dto.status(), dto.description(),
                dto.date(), dto.category(), dto.type()
        );
    }
}
