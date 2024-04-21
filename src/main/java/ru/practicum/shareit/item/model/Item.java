package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@Builder
public class Item {
    @Positive
    private long id;

    @NotNull
    @NotBlank
    private String name;

    @Size(max = 100)
    private String description;

    private boolean available;

    private long ownerId;
}
