package ru.practicum.ewm.controllers.categories;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.markers.Create;
import ru.practicum.ewm.markers.Update;
import ru.practicum.ewm.services.CategoryService;

import static ru.practicum.ewm.controllers.categories.CategoryMapper.map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/categories")
public class CategoriesController {

    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(
            @Validated(Create.class) @RequestBody CategoryDto categoryDto) {
        Category category = map(categoryDto);
        return map(service.create(category));
    }

    @PatchMapping("{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(
            @PathVariable long categoryId,
            @Validated(Update.class) @RequestBody CategoryDto categoryDto) {
        Category category = map(categoryDto).toBuilder()
                .id(categoryId)
                .build();
        return map(service.update(category));
    }

    @DeleteMapping("{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long categoryId) {
        service.delete(categoryId);
    }

}
