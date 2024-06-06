package ru.practicum.shareit.booking;

import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.common.BaseClient;

public class BookingClient extends BaseClient {
    public BookingClient(RestTemplate rest) {
        super(rest);
    }
}
