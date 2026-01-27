package nttdata.personal.julius.api.adapter.controller;

import jakarta.validation.Valid;
import nttdata.personal.julius.api.application.dto.LoginRequestDto;
import nttdata.personal.julius.api.application.dto.LoginResponseDto;
import nttdata.personal.julius.api.application.dto.UserDto;
import nttdata.personal.julius.api.application.dto.UserResponseDto;
import nttdata.personal.julius.api.application.service.AuthService;
import nttdata.personal.julius.api.application.service.UserService;
import nttdata.personal.julius.api.adapter.dto.LoginRequest;
import nttdata.personal.julius.api.adapter.dto.LoginResponse;
import nttdata.personal.julius.api.adapter.dto.UserRequest;
import nttdata.personal.julius.api.adapter.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest request) {
        UserDto dto = new UserDto(request.name(), request.email(), request.cpf(), request.password());
        
        UserResponseDto responseDto = userService.create(dto);
        
        UserResponse response = new UserResponse(responseDto.id(), responseDto.name(), responseDto.email());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginRequestDto dto = new LoginRequestDto(request.email(), request.password());
        LoginResponseDto responseDto = authService.login(dto);
        return ResponseEntity.ok(new LoginResponse(responseDto.token()));
    }
}
