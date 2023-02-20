package ru.practicum.ewm.controllers.categories;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.entities.Category;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

    public static Category map(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .build();
    }

    public static CategoryDto map(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
