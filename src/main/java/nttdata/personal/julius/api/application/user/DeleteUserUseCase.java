package nttdata.personal.julius.api.application.user;

import nttdata.personal.julius.api.application.user.dto.UserResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.user.User;
import nttdata.personal.julius.api.domain.user.UserRepository;

import java.util.UUID;

public class DeleteUserUseCase {
    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse execute(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        user.deactivate();
        userRepository.save(user);

        return UserResponse.fromDomain(user);
    }
}
