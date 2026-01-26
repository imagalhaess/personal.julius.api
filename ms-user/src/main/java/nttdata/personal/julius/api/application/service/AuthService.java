package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.dto.LoginRequestDto;
import nttdata.personal.julius.api.application.dto.LoginResponseDto;
import nttdata.personal.julius.api.domain.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.User;
import nttdata.personal.julius.api.domain.repository.UserRepository;
import nttdata.personal.julius.api.infrastructure.security.JwtService;
import nttdata.personal.julius.api.infrastructure.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception e) {
            throw new BusinessException("Credenciais inválidas");
        }

        User user = userRepository.findByEmail(request.email())
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado ou inativo"));

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new LoginResponseDto(token);
    }
}
