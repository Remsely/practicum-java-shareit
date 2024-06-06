package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.BaseClient;

import java.util.HashMap;
import java.util.Map;


@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                        .build()
        );
    }

    public ResponseEntity<?> postBooking(BookingCreationDto dto, Long userId) {
        return post("/", userId, dto);
    }

    public ResponseEntity<?> patchBooking(long id, Long userId, Boolean approved) {
        Map<String, Object> params = Map.of("approved", approved);
        return patch("/" + id, userId, params);
    }

    public ResponseEntity<?> getBooking(long id, Long userId) {
        return get("/" + id, userId);
    }

    public ResponseEntity<?> getUserBookings(Long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> params = getRequestParams(state, from, size);
        return get("/", userId, params);
    }

    public ResponseEntity<?> getUserItemsBookings(Long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> params = getRequestParams(state, from, size);
        return get("/owner", userId, params);
    }

    private Map<String, Object> getRequestParams(BookingState state, Integer from, Integer size) {
        Map<String, Object> params = new HashMap<>();
        if (from != null && size != null) {
            params.put("from", from);
            params.put("size", size);
        }
        params.put("state", state);
        return params;
    }
}
