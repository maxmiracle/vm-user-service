# vm-user-service
Сервис данных пользователя

### Создаем openAPI сервиса
Для создания openAPI rest-интерфейса воспользуемся редактором swagger-editor.  
Можно использовать ресурс в интернете  
https://editor.swagger.io/  
Альтернативой может быть развертывания сервиса swagger-editor в docker локально, чтобы не посылать свои файлы в интернет.
https://hub.docker.com/r/swaggerapi/swagger-editor/  

### Создаем проект
Для создания проекта воспользуемся генератором проекта spring initializer.
https://start.spring.io/  
Мы решили, что мы хотим использовать JDK 21 и реактивный стек,  
кроме того для доступа к БД будем использовать JOOQ, как будто у нас сложные запросы с фильтрами и соединениями таблиц.  
- Для упрощения генерации POJO добавим Lombok.
- Spring Reactive Web, реактивный контроллер на основе Netty сервера.
- JOOQ Access Layer - для доступа к БД. Хорошо, что JOOQ умеет использовать реактивный драйвер. (Мы могли бы использовать Spring Data R2DBC, это описано в документации Spring, для Reactor)
- PostgresSQL Driver – для подключения к БД Postgres. Подмечаем, что он умеет делать R2DBC.
- Liquibase Migration – для создания структуры БД, тут нам не нужна реактивность.
- Spring Boot Actuator – для поддержки работы в кластере
- Prometheus – для мониторинга сервиса
- Testcontainers - для создания авто тестов в сервисе с использованием БД Postgres, расположенной в контейнере
- Еще мы хотим swagger-ui для тестировщиков, но его нет в spring initializer, поэтому добавим его вручную.

### Генерируем rest api из описания openAPI.yaml

Добавляем плагин ```id 'org.openapi.generator' version '7.14.0'```
Настраиваем генерацию файлов с помощью gradle task, которая будет запускаться перед compileJava.
```groovy gradle
compileJava.dependsOn "openApiGenerate"
openApiGenerate {
    Directory outputGenerated = layout.buildDirectory.dir("generated").get()
    generatorName.set("spring")
    inputSpec.set("$rootDir/src/main/resources/specification/openapi-users.yaml")
    outputDir.set("$outputGenerated/openapi")
    apiPackage.set("ru.maximserver.vmuserservice.api")
    modelPackage.set("ru.maximserver.vmuserservice.model")
    configOptions.set([library                             : "spring-boot",
                       useOptional                         : "true",
                       openApiNullable                     : "false",
                       interfaceOnly                       : "true",
                       generatedConstructorWithRequiredArgs: "false",
                       useTags                             : "true",
                       basePackage                         : "ru.maximserver.vmuserservice",
                       useJakartaEe                        : "true",
                       reactive                            : "true"])
}
```
Теперь при запуске сборки build или отдельного запуска task  ```openApiGenerate``` будут созадваться исходные коды 
в папке build/generated/openapi. 
Добавим эту папку в список исходников:
```groovy gradle
sourceSets {
    main {
        java {
            srcDirs += ["$project.buildDir/generated/openapi/src/main/java"]
        }
    }
}
```

### Сделаем заготовки файлов с помощью ИИ, чтобы меньше набирать кода.
Воспользуемся сервисом GIGA CODE.
```gigacode
Сгенерируй, используя описание REST API из файла @openapi-users.yaml: 
- Интерфейс rest контроллера
- Классы dto объектов rest
- Реализация rest контроллера
- Скрипт Liquibase для Data Layer этого контроллера
- Data Layer с использованием JOOQ
Особенности реализации:
- Классы разместить в пакете ru.maximserver.vmuserservice. 
- Для dto объектов использовать Lombok annotations. 
- Интерфейс аннатационного rest контроллера необходимо сделать с использованием WebFlux с добавлением аннотаций для swagger io.swagger.v3.oas.annotations.*
- Использованы **Swagger 3 (OpenAPI 3)** аннотации: `@Operation`, `@ApiResponse`, `@Parameter`, `@Tag`, `@Schema`, `@Content`, `@ExampleObject`.
- Все примеры из OpenAPI (например, `sample-user`) перенесены в аннотации.
- Поддержка **WebFlux** через `Mono<ResponseEntity<T>>`.
- DTO-классы содержат `@Schema` с описаниями и примерами — это улучшает документацию в Swagger UI.
- Поля с примерами и ограничениями (например, `maxLength`, `pattern`) аннотированы соответствующим образом.
- Добавить Конфигурацию OpenAPI (в `application.yml`)
- Добавить валидацию (`@Valid`, `@NotBlank`, `@Email`, `@Pattern`)
```
Однако, большинство полученных файлов будет использовано с редакцией. 
Важно понимать и помнить, что ИИ может пропустить важный аспект реализации.

### На основе сгенерированного gigacode создаем liquibase скрипт.
Скрипт размещается в ресурсах проекта в папке ```src/main/resources/db/changelog```
Скрипт состоит из основного файла, который указывает, какие файлы входят в состав главного.
Библиотека liquibase позволяет создавать структуру БД и управлять изменениями этой структуры.
По сути это DDL, который может быть представлен xml, yaml или непосредственно sql скриптами.
Также liquibase используется в тестовых сценариях вместе с библиотекой testcontainers.  
Теперь тест сгенерированный spring initializer, который запускает приложение, отрабатывает без ошибок, 
так как liquibase теперь генерирует БД и сервис может к ней подключиться. 


### На основе сгенерированного скрипта Liquibase генерируем JOOQ metadata classes.
Подключаем плагин gradle plugin
```code
plugins {
    ...
    id 'org.jooq.jooq-codegen-gradle' version '3.19.17'
```
Добавляем dependencies
```groovy
jooqCodegen "org.jooq:jooq-meta-extensions-liquibase:3.19.17"
```
Добавляем настройки gradle task, ставим зависимость для компиляции (compileJava) от этого нового task.
```groovy
compileJava.dependsOn "jooqCodegen"
jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                properties {
                    property {
                        key = "rootPath"
                        value = "$rootDir/src/main/resources"
                    }
                    property {
                        key = "scripts"
                        value = "/db/changelog/db.changelog-master.yaml"
                    }
                    property {
                        key = "includeLiquibaseTables"
                        value = false
                    }
                    property {
                        key = "liquibaseSchemaName"
                        value = "public"
                    }
                    property {
                        key = "changeLogParameters.contexts"
                        value = "!test"
                    }
                }
            }
            target {
                packageName = "ru.maximserver.vmuserservice.jooq.gen"
            }
        }
    }
}
```
При запуске task jooqCodegen сгенерируется код с метаданными БД для библиотеки JOOQ. 
JOOQ также может сгенерировать метаданные из БД, но тогда необходимо поднимать instance БД, например в докере.
Такие проекты и сценарии существуют и поддерживаются в некоторых компаниях/командах/проектах.
Решение с генерацией jooq из скрипта liquibase элегантное, но имеет ряд ограничений.
Одним из вариантов может быть фиксация метафайлов jooq в репозитории проекта (git), а не генерация их на лету с помощью gradle task, как было показано на нашем примере.
Теперь перед compileJava у нас запускается как openApiGenerate, так и jooqCodegen.

### Реализация rest-контроллера
Интерфейс контроллера уже сгенерирован, нужно лишь определить реализацию, в которой основным действием будет вызов сервисного слоя.
Интерфейс генерируется в файле build/generated/openapi/src/main/java/ru/maximserver/vmuserservice/api/UserApi.java.
Реализуем этот интерфейс в новом классе UserService
```java
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
```

### Реализация сервисного слоя

### Реализация маппера

### Реализация конфигурации JOOQ, Swagger

### Интеграционные тесты

