package ru.practicum.shareit.common.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.IllegalPageableArgumentsException;
import ru.practicum.shareit.exception.model.ErrorResponse;

@Component
public class PageableUtility {
    private boolean isPageableArgumentsNull(Integer from, Integer size) {
        return from == null || size == null;
    }

    private void checkPageableArguments(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new IllegalPageableArgumentsException(
                    ErrorResponse.builder()
                            .reason("ItemRequestService get all requests")
                            .error("Arguments from: " + from + ", size: " + size + ".")
                            .build()
            );
        }
    }

    public Pageable getPageableFromArguments(Integer from, Integer size) {
        Pageable pageable;
        if (isPageableArgumentsNull(from, size)) {
            pageable = Pageable.unpaged();
        } else {
            checkPageableArguments(from, size);
            int page = from / size;
            pageable = PageRequest.of(page, size);
        }
        return pageable;
    }
}
