package ru.practicum.shareit.unit.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.utils.PageableUtility;
import ru.practicum.shareit.exception.IllegalPageableArgumentsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class PageableUtilityTest {
    @InjectMocks
    private PageableUtility pageableUtility;

    @Test
    public void testGetPageableFromArguments_FailArguments() {
        assertThrows(IllegalPageableArgumentsException.class, () ->
                pageableUtility.getPageableFromArguments(-1, 2));
        assertThrows(IllegalPageableArgumentsException.class, () ->
                pageableUtility.getPageableFromArguments(2, 0));
        assertThrows(IllegalPageableArgumentsException.class, () ->
                pageableUtility.getPageableFromArguments(2, -1));
    }

    @Test
    public void testGetPageableFromArguments_NullArguments() {
        assertEquals(pageableUtility.getPageableFromArguments(null, null), Pageable.unpaged());
    }

    @Test
    public void testGetPageableFromArguments_Success() {
        assertEquals(pageableUtility.getPageableFromArguments(4, 2), PageRequest.of(2, 2));
    }
}
