package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class ItemCreateDto {
    // Отличие этого dto в аннотациях. Мне другой ревьювер писал, что лучше выносить валидацию в аннотации, чем писать
    // для нее отдельные методы. Поэтому решил сделать так.
    // Если все же лучше написать метод для валидации, верните работу, исправлю. :)
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    private Boolean available;
}
