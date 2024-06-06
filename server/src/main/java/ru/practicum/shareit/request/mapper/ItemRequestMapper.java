package ru.practicum.shareit.request.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequest toEntity(ItemRequestCreationDto dto) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .build();
    }

    public ItemRequestDto toDto(ItemRequest request) {
        List<Item> items = request.getItems();
        return ItemRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .description(request.getDescription())
                .items(items == null ? null : itemMapper.toDtoList(items))
                .build();
    }

    public List<ItemRequestDto> toDtoList(List<ItemRequest> requests) {
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
