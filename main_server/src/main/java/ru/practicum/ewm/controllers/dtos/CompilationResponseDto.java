package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CompilationResponseDto {

    @Positive
    private Long id;

    @NotBlank
    private String title;

    @NotNull
    private Boolean pinned;

    private List<EventDto> events;
}
