package ru.maximserver.vmuserservice.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.maximserver.vmuserservice.api.UserApi;
import ru.maximserver.vmuserservice.model.UpdateUser;
import ru.maximserver.vmuserservice.model.User;
import ru.maximserver.vmuserservice.service.UserService;

@RestController
@RequestMapping("${openapi.user-service.base-path:/}")
@RequiredArgsConstructor
public class UserApiController implements UserApi {

    private final UserService userService;

    @Override
    public Mono<@NonNull ResponseEntity<Void>> createUser(
            final Long userId,
            final Mono<@NonNull UpdateUser> updateUser,
            final ServerWebExchange exchange
    ){
        return userService.createUser(updateUser, userId)
                .then(Mono.just(ResponseEntity.status(HttpStatus.CREATED).build()));
    }

    @Override
    public Mono<@NonNull ResponseEntity<Void>> deleteUser(
            final Long userId,
            final ServerWebExchange exchange){
        return userService.deleteUser(userId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<@NonNull ResponseEntity<@NonNull User>> findUserById(
            final Long userId,
            final ServerWebExchange exchange) {
        return userService.findUserById(userId).map(ResponseEntity::ok);
    }

    @Override
    public Mono<@NonNull ResponseEntity<Void>> updateUser(
            final Long userId,
            final Mono<@NonNull UpdateUser> user,
            final ServerWebExchange exchange
    ){
        return userService.updateUser(userId, user)
                .thenReturn(ResponseEntity.noContent().build());
    }

}

