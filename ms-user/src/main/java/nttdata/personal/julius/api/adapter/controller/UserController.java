package nttdata.personal.julius.api.adapter.controller;

import jakarta.validation.Valid;
import nttdata.personal.julius.api.application.dto.UserResponseDto;
import nttdata.personal.julius.api.application.dto.UserUpdateDto;
import nttdata.personal.julius.api.application.service.UserService;
import nttdata.personal.julius.api.adapter.dto.UserResponse;
import nttdata.personal.julius.api.adapter.dto.UserUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listAll() {
        List<UserResponseDto> usersDto = userService.findAll();

        List<UserResponse> responseList = usersDto.stream()
                .map(dto -> new UserResponse(dto.id(), dto.name(), dto.email()))
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        UserResponseDto dto = userService.getUser(id);
        UserResponse response = new UserResponse(dto.id(), dto.name(), dto.email());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody @Valid UserUpdateRequest request) {
        UserUpdateDto dto = new UserUpdateDto(id, request.name(), request.email(), request.cpf());
        UserResponseDto responseDto = userService.update(dto);
        return ResponseEntity.ok(toResponse(responseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(UserResponseDto dto) {
        return new UserResponse(dto.id(), dto.name(), dto.email());
    }
}