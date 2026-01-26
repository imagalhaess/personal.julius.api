package nttdata.personal.julius.api.application.dto;

import java.util.UUID;

public record UserUpdateDto(
        UUID id,
        String name,
        String email,
        String cpf
) {}
