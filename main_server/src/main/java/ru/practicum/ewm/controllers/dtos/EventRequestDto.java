package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.entities.EventRequestState;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class EventRequestDto {

    @Positive
    private Long id;

    @NotBlank
    private String created;

    @NotNull
    private EventRequestState status;

    @Positive
    private Long event;

    @Positive
    private Long requester;
}
