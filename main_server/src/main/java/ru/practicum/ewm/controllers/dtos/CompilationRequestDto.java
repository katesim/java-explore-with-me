package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.markers.Create;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CompilationRequestDto {

    @NotBlank(groups = {Create.class})
    private String title;

    private Boolean pinned;

    private List<Long> events;
}
