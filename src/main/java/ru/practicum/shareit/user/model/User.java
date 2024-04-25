package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class User {
    private long id;

    @NotNull
    private String name;

    @NotNull
    @Email
    private String email;
}
