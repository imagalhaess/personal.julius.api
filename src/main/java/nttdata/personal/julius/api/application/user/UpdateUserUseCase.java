package nttdata.personal.julius.api.application.user;

import nttdata.personal.julius.api.application.user.dto.UserRequest;
import nttdata.personal.julius.api.application.user.dto.UserResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.user.Email;
import nttdata.personal.julius.api.domain.user.User;
import nttdata.personal.julius.api.domain.user.UserRepository;

import java.util.UUID;

public class UpdateUserUseCase {
    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse execute(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        Email newEmail = new Email(request.email());

        var existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            throw new BusinessException("Este e-mail já está sendo usado por outro usuário.");
        }

        user.setName(request.name());
        user.setEmail(newEmail);
        userRepository.save(user);

        return UserResponse.fromDomain(user);
    }
}
