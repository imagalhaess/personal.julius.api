package nttdata.personal.julius.api.adapter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nttdata.personal.julius.api.adapter.dto.UserResponse;
import nttdata.personal.julius.api.adapter.dto.UserUpdateRequest;
import nttdata.personal.julius.api.application.dto.ImportReportDto;
import nttdata.personal.julius.api.application.service.UserImportService;
import nttdata.personal.julius.api.application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UserController {

    private final UserService userService;
    private final UserImportService userImportService;

    public UserController(UserService userService, UserImportService userImportService) {
        this.userService = userService;
        this.userImportService = userImportService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários ativos")
    public ResponseEntity<List<UserResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes de um usuário pelo ID público")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de um usuário")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar um usuário")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar usuários via planilha Excel")
    public ResponseEntity<ImportReportDto> importUsers(@RequestParam("file") MultipartFile file) {
        ImportReportDto report = userImportService.importUsers(file);
        return ResponseEntity.ok(report);
    }
}
