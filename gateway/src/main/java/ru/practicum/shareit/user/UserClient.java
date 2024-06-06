package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.BaseClient;

@Slf4j
@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                        .build()
        );
    }

    public ResponseEntity<?> postUser(UserCreationDto dto) {
        logSending();
        return post("/", dto);
    }

    public ResponseEntity<?> patchUser(UserDto dto, long id) {
        logSending();
        return patch("/" + id, dto);
    }

    public ResponseEntity<?> getUser(long id) {
        logSending();
        return get("/" + id);
    }

    public ResponseEntity<?> deleteUser(long id) {
        logSending();
        return delete("/" + id);
    }

    public ResponseEntity<?> getUsers() {
        logSending();
        return get("/");
    }
}
