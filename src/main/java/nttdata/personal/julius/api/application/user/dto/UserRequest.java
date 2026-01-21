package nttdata.personal.julius.api.application.user.dto;

public record UserRequest(
        String name,
        String email,
        String cpf,
        String password) {
}
