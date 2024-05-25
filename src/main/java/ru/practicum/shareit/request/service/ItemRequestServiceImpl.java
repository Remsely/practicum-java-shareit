package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.utils.PageableUtility;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserJpaRepository userRepository;
    private final PageableUtility pageableUtility;

    @Override
    public ItemRequest addRequest(ItemRequest request, long userId) {
        request.setUser(findUser(userId));
        request.setCreated(LocalDateTime.now());
        ItemRequest savedRequest = requestRepository.save(request);
        log.info("add ItemRequest: an item request with an id {} and owner id {} has been added. ItemRequest : {}.",
                savedRequest.getId(), userId, savedRequest);
        return savedRequest;
    }

    @Override
    public List<ItemRequest> getUserRequests(long userId) {
        User user = findUser(userId);
        List<ItemRequest> foundRequests = requestRepository.findByUser(user);
        log.info("get user's ItemRequest: the list of item requests of the user with id {} has been received. " +
                "List (size = {}) : {}.", userId, foundRequests.size(), foundRequests);
        return foundRequests;
    }

    @Override
    public List<ItemRequest> getAllRequests(Integer from, Integer size, long userId) {
        User user = findUser(userId);
        Pageable pageable = pageableUtility.getPageableFromArguments(from, size);
        List<ItemRequest> foundRequests = requestRepository.findByUserNot(user, pageable);
        log.info("get not user's ItemRequest: the list of item requests of the user with id {} has been " +
                "received. List (size = {}) : {}.", userId, foundRequests.size(), foundRequests);
        return foundRequests;
    }

    @Override
    public ItemRequest getRequest(long requestId, long userId) {
        checkUserExistence(userId);
        ItemRequest request = findRequest(requestId);
        log.info("get ItemRequest: an item request with an id {} has been found. ItemRequest : {}.",
                requestId, request);
        return request;
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + userId + " does not exist!")
                                .build()
                ));
    }

    private ItemRequest findRequest(long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("ItemRequest repository")
                                .error("Request with id " + requestId + " does not exist!")
                                .build()
                ));
    }

    private void checkUserExistence(long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(ErrorResponse.builder()
                    .reason("User repository")
                    .error("User with id " + id + " does not exist!")
                    .build()
            );
        }
    }
}
