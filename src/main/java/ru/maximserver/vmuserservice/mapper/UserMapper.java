package ru.maximserver.vmuserservice.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.maximserver.vmuserservice.jooq.gen.tables.records.UserAccountRecord;
import ru.maximserver.vmuserservice.model.UpdateUser;
import ru.maximserver.vmuserservice.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target="id", source="userId")
    @Mapping(target=".", source="user")
    UserAccountRecord toUserAccountRecord(UpdateUser user, Long userId);

    User toUser(UserAccountRecord userAccountRecord);
}
