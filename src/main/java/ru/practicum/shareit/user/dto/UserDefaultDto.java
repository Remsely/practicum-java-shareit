package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
@Builder
public class UserDefaultDto {
    private long id;

    private String name;

    @Email
    private String email;
}
