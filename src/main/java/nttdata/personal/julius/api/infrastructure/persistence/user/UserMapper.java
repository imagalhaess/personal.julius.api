package nttdata.personal.julius.api.infrastructure.persistence.user;

import nttdata.personal.julius.api.domain.user.Cpf;
import nttdata.personal.julius.api.domain.user.Email;
import nttdata.personal.julius.api.domain.user.User;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                new Cpf(entity.getCpf()),
                entity.getPassword(),
                entity.getCreatedAt(),
                entity.isActive()
        );
    }

    public static UserEntity toEntity(User domain) {
        return new UserEntity(
                domain.getId(),
                domain.getName(),
                domain.getEmail().email(),
                domain.getCpf().value(),
                domain.getPasswordHash(),
                domain.getCreatedAt(),
                domain.isActive()
        );
    }
}
