package nttdata.personal.julius.api.application.dto;

public record UserDto(
        String name,
        String email,
        String cpf,
        String password
) {}
