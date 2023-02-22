package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class LocationDto {

    @NotNull
    private Float lon;

    @NotNull
    private Float lat;
}
