package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.adapter.dto.UserRequest;
import nttdata.personal.julius.api.adapter.dto.UserResponse;
import nttdata.personal.julius.api.adapter.dto.UserUpdateRequest;
import nttdata.personal.julius.api.common.exception.BusinessException;
import nttdata.personal.julius.api.domain.model.User;
import nttdata.personal.julius.api.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(UserRequest dto) {
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

    public UserResponse getUser(Long id) {
        User user = repository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        return toResponse(user);
    }

    public UserResponse update(Long id, UserUpdateRequest dto) {
        User user = repository.findById(id)
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

    public void delete(Long id) {
        User user = repository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado ou já inativo"));

        user.deactivate();
        repository.save(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public List<UserResponse> findAll(int page, int size) {
        return repository.findAllActive(page, size)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}