package nttdata.personal.julius.api.adapter.dto;

public record LoginResponse(
        String token,
        Long id,
        String name,
        String email
) {}
