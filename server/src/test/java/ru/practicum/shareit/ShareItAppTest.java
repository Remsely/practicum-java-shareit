package ru.practicum.shareit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ShareItAppTest {
    @Test
    public void testMain() {
        assertDoesNotThrow(ShareItApp::new);
        assertDoesNotThrow(() -> ShareItApp.main(new String[0]));
    }
}
