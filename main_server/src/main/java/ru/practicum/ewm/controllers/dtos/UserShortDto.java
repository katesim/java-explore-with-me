package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserShortDto {

    @Positive
    private Long id;

    @NotBlank
    private String name;
}
