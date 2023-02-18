package ru.practicum.ewm.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class StatsDto {
    @NotBlank
    private String app;
    @NotBlank
    private String uri;
    @PositiveOrZero
    private Long hits;
}
