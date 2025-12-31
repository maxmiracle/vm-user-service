package ru.maximserver.vmuserservice.controller;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.maximserver.vmuserservice.BaseIntegrationTest;
import ru.maximserver.vmuserservice.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.maximserver.vmuserservice.jooq.gen.Tables.USER_ACCOUNT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class UserApiControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DSLContext dslContext;

    @Test
    public void createUser() {
        User user = new User();
        user.setUsername("johndoe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("johndoe@mail.com");
        user.setPhone("89000000000");
        webTestClient.post()
                .uri("/user/1")
                .body(Mono.just(user), User.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated();

        StepVerifier.create(dslContext.selectFrom(USER_ACCOUNT).where(USER_ACCOUNT.USERNAME.eq("johndoe")))
                .assertNext(userAccountRecord -> {
                    assertThat(userAccountRecord).isNotNull();
                    assertThat(userAccountRecord.getId()).isEqualTo(1);
                    assertThat(userAccountRecord.getFirstName()).isEqualTo("John");
                    assertThat(userAccountRecord.getLastName()).isEqualTo("Doe");
                    assertThat(userAccountRecord.getEmail()).isEqualTo("johndoe@mail.com");
                    assertThat(userAccountRecord.getPhone()).isEqualTo("89000000000");
                })
                .expectComplete()
                .verify();
    }
}
