package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.controllers.dtos.CategoryDto;
import ru.practicum.ewm.controllers.mappers.CategoryMapper;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.markers.Create;
import ru.practicum.ewm.markers.Update;
import ru.practicum.ewm.services.CategoryService;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.common.EWMConstants.PAGE_SIZE_DEFAULT_TEXT;
import static ru.practicum.ewm.common.EWMConstants.PAGE_START_FROM_DEFAULT_TEXT;
import static ru.practicum.ewm.controllers.mappers.CategoryMapper.map;

@RestController
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoryService service;

    // Admin

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(
            @Validated(Create.class) @RequestBody CategoryDto categoryDto) {
        Category category = map(categoryDto);
        return map(service.create(category));
    }

    @PatchMapping("/admin/categories/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(
            @PathVariable long categoryId,
            @Validated(Update.class) @RequestBody CategoryDto categoryDto) {
        Category category = map(categoryDto).toBuilder()
                .id(categoryId)
                .build();
        return map(service.update(category));
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long categoryId) {
        service.delete(categoryId);
    }

    // Public

    @GetMapping("/categories/{categoryId}")
    public CategoryDto getById(@PathVariable long categoryId) {
        return map(service.get(categoryId));
    }

    @GetMapping("/categories")
    public List<CategoryDto> getAll(
            @RequestParam(defaultValue = PAGE_START_FROM_DEFAULT_TEXT, required = false) @Min(0) int from,
            @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_TEXT, required = false) @Min(1) int size) {
        return service.getAll(from, size).stream()
                .map(CategoryMapper::map)
                .collect(Collectors.toList());
    }
}
