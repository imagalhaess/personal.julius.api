package nttdata.personal.julius.api.adapter.controller;

import jakarta.validation.Valid;
import nttdata.personal.julius.api.application.dto.ImportReportDto;
import nttdata.personal.julius.api.application.service.UserImportService;
import nttdata.personal.julius.api.application.service.UserService;
import nttdata.personal.julius.api.adapter.dto.UserResponse;
import nttdata.personal.julius.api.adapter.dto.UserUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserImportService userImportService;

    public UserController(UserService userService, UserImportService userImportService) {
        this.userService = userService;
        this.userImportService = userImportService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportReportDto> importUsers(@RequestParam("file") MultipartFile file) {
        ImportReportDto report = userImportService.importUsers(file);

        return ResponseEntity.ok(report);
    }
}