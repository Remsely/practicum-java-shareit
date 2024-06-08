package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                        .build()
        );
    }

    public ResponseEntity<?> postItem(ItemCreationDto dto, Long userId) {
        return post("/", userId, dto);
    }

    public ResponseEntity<?> patchItem(ItemDto dto, long id, Long userId) {
        return patch("/" + id, userId, dto);
    }

    public ResponseEntity<?> getItem(long id, Long userId) {
        return get("/" + id, userId);
    }

    public ResponseEntity<?> getItems(Long userId, Integer from, Integer size) {
        if (from == null || size == null) {
            return get("/", userId);
        }
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?size={size}&from={from}", userId, parameters);
    }

    public ResponseEntity<?> searchItems(Long userId, String text, Integer from, Integer size) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("text", text);
        if (from != null && size != null) {
            parameters.put("from", from);
            parameters.put("size", size);
        }
        log.info(parameters.toString());
        return get("/search", userId, parameters);
    }

    public ResponseEntity<?> postComment(CommentDto dto, long id, Long userId) {
        return post("/" + id + "/comment", userId, dto);
    }
}
