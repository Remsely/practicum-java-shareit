package ru.practicum.shareit.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestUtility {
    public static String getStringFromDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return formatter.format(date);
    }
}
