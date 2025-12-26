# vm-user-service
Сервис данных пользователя

### Сделаем заготовки файлов с помощью ИИ, чтобы меньше набирать кода.
Воспользуемся сервисом GIGA CODE.
```gigacode
Сгенерируй используя описание REST API из файла @openapi-users.yaml: 
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