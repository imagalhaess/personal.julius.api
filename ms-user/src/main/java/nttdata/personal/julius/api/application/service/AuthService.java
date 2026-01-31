package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.adapter.dto.LoginRequest;
import nttdata.personal.julius.api.adapter.dto.LoginResponse;
import nttdata.personal.julius.api.common.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.User;
import nttdata.personal.julius.api.domain.repository.UserRepository;
import nttdata.personal.julius.api.infrastructure.security.TokenService;
import nttdata.personal.julius.api.infrastructure.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            log.warn("Falha de autenticação para email={}: {}", request.email(), e.getMessage());
            throw new BusinessException("Credenciais inválidas", "INVALID_CREDENTIALS");
        } catch (Exception e) {
            log.error("Erro inesperado na autenticação para email={}", request.email(), e);
            throw new BusinessException("Erro interno na autenticação", "AUTH_ERROR");
        }

        User user = userRepository.findByEmail(request.email())
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado ou inativo"));

        String token = tokenService.generateToken(new UserPrincipal(user));
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}
