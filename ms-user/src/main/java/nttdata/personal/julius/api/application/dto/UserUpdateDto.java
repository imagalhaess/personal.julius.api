package nttdata.personal.julius.api.application.dto;

public record UserUpdateDto(
        Long id,
        String name,
        String email,
        String cpf
) {}
