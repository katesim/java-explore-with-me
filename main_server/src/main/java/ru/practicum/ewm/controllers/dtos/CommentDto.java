package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class CommentDto {

    @Positive
    private Long id;

    @Positive
    private Long eventId;

    @NotNull
    private UserShortDto user;

    @NotBlank
    private String createdOn;

    @NotBlank
    private String editedOn;

    @NotBlank
    private String text;
}
