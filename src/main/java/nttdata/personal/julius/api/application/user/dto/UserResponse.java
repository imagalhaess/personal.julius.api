package nttdata.personal.julius.api.application.user.dto;

import nttdata.personal.julius.api.domain.user.User;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email, boolean active) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail().email(),
                user.isActive()
        );
    }
}
