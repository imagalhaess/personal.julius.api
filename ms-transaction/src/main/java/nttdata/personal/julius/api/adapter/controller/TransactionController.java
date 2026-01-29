package nttdata.personal.julius.api.adapter.controller;

import jakarta.validation.Valid;
import nttdata.personal.julius.api.application.service.TransactionService;
import nttdata.personal.julius.api.adapter.dto.BalanceResponse;
import nttdata.personal.julius.api.adapter.dto.TransactionRequest;
import nttdata.personal.julius.api.adapter.dto.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        if (request.userId() != null) {
            userId = request.userId();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.withUserId(userId)));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.list(userId, page, size));
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.getBalance(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid TransactionRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.update(id, request.withUserId(userId)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        service.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        service.reject(id, reason);
        return ResponseEntity.ok().build();
    }
}
