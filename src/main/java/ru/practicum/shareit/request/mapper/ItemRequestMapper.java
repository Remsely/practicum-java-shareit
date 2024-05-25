package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemRequestResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {
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
                .items(items == null ? null : toRequestResponseDtoList(items))
                .build();
    }

    public List<ItemRequestDto> toDtoList(List<ItemRequest> requests) {
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ItemRequestResponseDto toRequestResponseDto(Item item) {
        return ItemRequestResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .requestId(item.getRequest().getId())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    private List<ItemRequestResponseDto> toRequestResponseDtoList(List<Item> items) {
        return items.stream()
                .map(this::toRequestResponseDto)
                .collect(Collectors.toList());
    }
}
