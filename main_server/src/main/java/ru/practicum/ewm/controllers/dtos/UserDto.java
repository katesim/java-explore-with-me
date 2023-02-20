package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.markers.Create;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {

    @Positive
    private Long id;

    @NotBlank(groups = Create.class)
    private String name;

    @Email(groups = Create.class)
    @NotBlank(groups = Create.class)
    private String email;
}
