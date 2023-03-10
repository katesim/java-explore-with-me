package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.entities.EventStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Getter
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventDto extends EventBaseDto {

    @Positive
    private Long id;

    @NotNull
    private EventStatus state;

    @NotNull
    private UserShortDto initiator;

    @NotNull
    private CategoryDto category;

    @NotBlank
    private String createdOn;

    @NotBlank
    private String publishedOn;

    @PositiveOrZero
    private Integer confirmedRequests;

    @PositiveOrZero
    private Long views;
}
