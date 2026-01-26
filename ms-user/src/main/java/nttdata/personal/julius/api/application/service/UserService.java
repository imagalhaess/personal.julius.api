package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.dto.UserDto;
import nttdata.personal.julius.api.application.dto.UserResponseDto;
import nttdata.personal.julius.api.application.dto.UserUpdateDto;
import nttdata.personal.julius.api.domain.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.User;
import nttdata.personal.julius.api.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto create(UserDto dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }
        if (repository.existsByCpf(dto.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        User user = new User(
                dto.name(),
                dto.email(),
                dto.cpf(),
                passwordEncoder.encode(dto.password())
        );

        User saved = repository.save(user);

        return toResponse(saved);
    }

    public UserResponseDto getUser(UUID id) {
        User user = repository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        return toResponse(user);
    }

                public UserResponseDto update(UserUpdateDto dto) {
                    User user = repository.findById(dto.id())
                            .filter(User::isActive)
                            .orElseThrow(() -> new BusinessException("Usuário não encontrado ou inativo"));
            
                    if (dto.email() != null && !dto.email().equals(user.getEmail())) {
                        if (repository.existsByEmail(dto.email())) {
                            throw new BusinessException("Este e-mail já está sendo usado por outro usuário.");
                        }
                    }

                    User updated = new User(
                            user.getId(),
                            dto.name() != null ? dto.name() : user.getName(),
                            dto.email() != null ? dto.email() : user.getEmail(),
                            user.getCpf(),
                            user.getPassword(),
                            user.isActive()
                    );
            
                    repository.save(updated);
                    return toResponse(updated);
                }    
        public void delete(UUID id) {
            User user = repository.findById(id)
                    .filter(User::isActive)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado ou já inativo"));
            
            user.deactivate();
            repository.save(user);
        }
    private UserResponseDto toResponse(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail());
    }
}