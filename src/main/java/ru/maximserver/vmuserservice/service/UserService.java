package ru.maximserver.vmuserservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.maximserver.vmuserservice.exception.ResourceNotFoundException;
import ru.maximserver.vmuserservice.mapper.UserMapper;
import ru.maximserver.vmuserservice.model.UpdateUser;
import ru.maximserver.vmuserservice.model.User;

import static ru.maximserver.vmuserservice.jooq.gen.tables.UserAccount.USER_ACCOUNT;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    private final DSLContext dslContext;

    public Mono<@NonNull Void> createUser(Mono<@NonNull UpdateUser> user, @NonNull final Long userId) {
        return user.map(userObject -> userMapper.toUserAccountRecord(userObject, userId))
                .flatMap(userAccountRecord ->
                        Mono.from(dslContext.insertInto(USER_ACCOUNT).set(userAccountRecord).returning()))
                .doOnNext(savedResult -> log.info("Inserted Record:\n{}", savedResult))
                .then();
    }

    public Mono<@NonNull Void> deleteUser(Long userId) {
      return Mono.from(dslContext.delete(USER_ACCOUNT)
                    .where(USER_ACCOUNT.ID.eq(userId)).returning())
              .switchIfEmpty(Mono.error(userNotFound(userId)))
              .doOnNext(savedResult -> log.info("Deleted Record:\n{}", savedResult))
              .then();
    }

    public Mono<@NonNull User> findUserById(Long userId) {
        return Mono.from(dslContext.selectFrom(USER_ACCOUNT)
                    .where(USER_ACCOUNT.ID.eq(userId)))
                .switchIfEmpty(Mono.error(userNotFound(userId)))
                .doOnNext(result -> log.info("Found Record:\n{}", result))
                .map(userMapper::toUser);
    }


    public Mono<@NonNull Void> updateUser(Long userId, Mono<@NonNull UpdateUser> user) {
        return user.map(userObject -> userMapper.toUserAccountRecord(userObject, userId))
                .flatMap(userAccountRecord -> Mono.from(dslContext.update(USER_ACCOUNT).set(userAccountRecord)
                        .where(USER_ACCOUNT.ID.eq(userId)).returning()))
                .switchIfEmpty(Mono.error(userNotFound(userId)))
                .doOnNext(savedResult -> log.info("Updated Record:\n{}", savedResult))
                .then();
    }

    private ResourceNotFoundException userNotFound(Long userId) {
        log.info("User {} not found", userId);
        return new ResourceNotFoundException("User not found");
    }
}
