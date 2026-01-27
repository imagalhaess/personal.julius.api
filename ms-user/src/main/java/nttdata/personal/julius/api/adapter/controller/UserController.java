package nttdata.personal.julius.api.adapter.controller;

import jakarta.validation.Valid;
import nttdata.personal.julius.api.application.dto.ImportReportDto;
import nttdata.personal.julius.api.application.dto.UserResponseDto;
import nttdata.personal.julius.api.application.dto.UserUpdateDto;
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
        List<UserResponseDto> usersDto = userService.findAll(page, size);

        List<UserResponse> responseList = usersDto.stream()
                .map(dto -> new UserResponse(dto.id(), dto.name(), dto.email()))
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponseDto dto = userService.getUser(id);
        UserResponse response = new UserResponse(dto.id(), dto.name(), dto.email());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        UserUpdateDto dto = new UserUpdateDto(id, request.name(), request.email(), request.cpf());
        UserResponseDto responseDto = userService.update(dto);
        return ResponseEntity.ok(toResponse(responseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(UserResponseDto dto) {
        return new UserResponse(dto.id(), dto.name(), dto.email());
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportReportDto> importUsers(@RequestParam("file") MultipartFile file) {
        ImportReportDto report = userImportService.importUsers(file);

        return ResponseEntity.ok(report);
    }
}