package ru.practicum.ewm.controllers.categories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.markers.Create;
import ru.practicum.ewm.markers.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CategoryDto {
    @Positive
    private Long id;
    @NotBlank(groups = {Create.class, Update.class})
    private String name;
}
