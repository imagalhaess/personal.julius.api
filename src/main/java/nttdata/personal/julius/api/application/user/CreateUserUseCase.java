package nttdata.personal.julius.api.application.user;

import nttdata.personal.julius.api.application.user.dto.UserRequest;
import nttdata.personal.julius.api.application.user.dto.UserResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.user.Cpf;
import nttdata.personal.julius.api.domain.user.Email;
import nttdata.personal.julius.api.domain.user.User;
import nttdata.personal.julius.api.domain.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CreateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserResponse execute(UserRequest request) {
        Email email = new Email(request.email());
        Cpf cpf = new Cpf(request.cpf());
        String encryptedPassword = passwordEncoder.encode(request.password());

        if (userRepository.findByCpf(cpf).isPresent()) {
            throw new BusinessException("CPF já cadastrado no sistema.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email já cadastrado no sistema.");
        }

        User user = new User(request.name(), email, cpf, encryptedPassword);
        userRepository.save(user);

        return UserResponse.fromDomain(user);
    }
}
