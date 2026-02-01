package nttdata.personal.julius.api.adapter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nttdata.personal.julius.api.adapter.dto.BalanceResponse;
import nttdata.personal.julius.api.adapter.dto.TransactionRequest;
import nttdata.personal.julius.api.adapter.dto.TransactionResponse;
import nttdata.personal.julius.api.application.service.ReportService;
import nttdata.personal.julius.api.application.service.TransactionService;
import nttdata.personal.julius.api.common.security.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transações", description = "Operações financeiras e relatórios")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService service;
    private final ReportService reportService;

    public TransactionController(TransactionService service, ReportService reportService) {
        this.service = service;
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Criar nova transação")
    public ResponseEntity<TransactionResponse> create(@RequestBody @Valid TransactionRequest request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.withUserId(userId)));
    }

    @GetMapping
    @Operation(summary = "Listar transações do usuário logado")
    public ResponseEntity<List<TransactionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(service.list(userId, page, size));
    }

    @GetMapping("/balance")
    @Operation(summary = "Obter saldo consolidado")
    public ResponseEntity<BalanceResponse> getBalance() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(service.getBalance(userId));
    }


    @GetMapping("/report/excel")
    @Operation(summary = "Baixar relatório financeiro em Excel")
    public ResponseEntity<byte[]> downloadExcel() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        byte[] excel = reportService.generateExcelReport(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/report/pdf")
    @Operation(summary = "Baixar relatório financeiro em PDF")
    public ResponseEntity<byte[]> downloadPdf() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        byte[] pdf = reportService.generatePdfReport(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma transação")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma transação")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid TransactionRequest request
    ) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(service.update(id, request.withUserId(userId)));
    }
}
