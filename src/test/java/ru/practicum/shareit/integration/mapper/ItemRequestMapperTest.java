package ru.practicum.shareit.integration.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class ItemRequestMapperTest {
    @Autowired
    private ItemRequestMapper mapper;

    @Test
    public void testToEntity() {
        ItemRequestCreationDto dto = ItemRequestCreationDto.builder()
                .description("description")
                .build();

        ItemRequest entity = mapper.toEntity(dto);

        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
    }

    @Test
    public void testToDto() {
        Item item = Item.builder()
                .id(1L)
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .description("description")
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto dto = mapper.toDto(request);

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getItems()).isNull();
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());

        request.setItems(List.of(item));

        dto = mapper.toDto(request);

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getItems().size()).isEqualTo(request.getItems().size());
        assertThat(dto.getItems().get(0).getId()).isEqualTo(request.getItems().get(0).getId());
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
    }

    @Test
    public void testToDtoList() {
        Item item = Item.builder()
                .id(1L)
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .description("description")
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();

        List<ItemRequest> requests = List.of(request, request, request);
        List<ItemRequestDto> dtos = mapper.toDtoList(requests);

        assertThat(dtos.size()).isEqualTo(3);
        assertThat(dtos.get(0).getId()).isEqualTo(request.getId());
        assertThat(dtos.get(0).getDescription()).isEqualTo(request.getDescription());
        assertThat(dtos.get(0).getItems().size()).isEqualTo(request.getItems().size());
        assertThat(dtos.get(0).getItems().get(0).getId()).isEqualTo(request.getItems().get(0).getId());
        assertThat(dtos.get(0).getCreated()).isEqualTo(request.getCreated());
    }
}
