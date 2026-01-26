package nttdata.personal.julius.api.adapter.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email
) {}
