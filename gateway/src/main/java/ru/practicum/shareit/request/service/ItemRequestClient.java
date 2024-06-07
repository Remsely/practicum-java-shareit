package ru.practicum.shareit.request.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                        .build()
        );
    }

    public ResponseEntity<?> postRequest(ItemRequestCreationDto dto, Long userId) {
        return post("/", userId, dto);
    }

    public ResponseEntity<?> getRequest(long requestId, Long userId) {
        return get("/" + requestId, userId);
    }

    public ResponseEntity<?> getUserRequests(Long userId) {
        return get("/", userId);
    }

    public ResponseEntity<?> getAllItemRequests(Long userId, Integer from, Integer size) {
        if (from != null && size != null) {
            Map<String, Object> params = Map.of(
                    "from", from,
                    "size", size
            );
            return get("/all", userId, params);
        }
        return get("/all", userId);
    }
}
