package ru.practicum.ewm.controllers.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.markers.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class EventBaseDto {

    @NotBlank(groups = Create.class)
    private String title;

    @NotBlank(groups = Create.class)
    private String description;

    @NotBlank(groups = Create.class)
    private String annotation;

    @NotBlank(groups = Create.class)
    private String eventDate;

    @NotNull(groups = Create.class)
    private LocationDto location;

    @Positive(groups = Create.class)
    private Integer participantLimit;

    @NotNull(groups = Create.class)
    private Boolean paid;

    @NotNull(groups = Create.class)
    private Boolean requestModeration;
}
