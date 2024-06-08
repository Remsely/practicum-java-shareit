package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ActiveProfiles("test")
public class ShareItAppTest {
    @Test
    public void testMain() {
        assertDoesNotThrow(ShareItApp::new);
        assertDoesNotThrow(() -> ShareItApp.main(new String[0]));
    }
}
