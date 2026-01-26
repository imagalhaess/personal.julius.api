package nttdata.personal.julius.api.application.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String name,
        String email
) {}
