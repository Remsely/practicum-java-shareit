package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class UserDto {
    @Positive
    private long id;

    @NotNull
    private String name;

    @Email
    private String email;
}
