package ru.practicum.shareit.unit.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.utils.PageableUtility;
import ru.practicum.shareit.exception.IllegalPageableArgumentsException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PageableUtilityTest {
    private final PageableUtility pageableUtility = new PageableUtility();

    private static Stream<Arguments> testGetPageableFromArgumentsArguments() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, 3),
                Arguments.of(3, null)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 2",
            "2, 0",
            "2, -1"
    })
    public void testGetPageableFromArguments_FailArguments(Integer from, Integer size) {
        assertThrows(IllegalPageableArgumentsException.class, () ->
                pageableUtility.getPageableFromArguments(from, size));
    }

    @ParameterizedTest
    @MethodSource("testGetPageableFromArgumentsArguments")
    public void testGetPageableFromArguments_NullArguments(Integer from, Integer size) {
        assertEquals(pageableUtility.getPageableFromArguments(from, size), Pageable.unpaged());
    }

    @Test
    public void testGetPageableFromArguments_Success() {
        assertEquals(pageableUtility.getPageableFromArguments(4, 2), PageRequest.of(2, 2));
    }
}
