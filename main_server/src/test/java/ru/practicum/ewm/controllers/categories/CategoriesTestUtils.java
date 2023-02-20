package ru.practicum.ewm.controllers.categories;

import ru.practicum.ewm.entities.Category;

public class CategoriesTestUtils {

    public static final Long CATEGORY_ID = 1L;
    public static final String NAME = "testName";

    public static Category getDefaultCategory() {
        return Category.builder()
                .id(CATEGORY_ID)
                .name(NAME)
                .build();
    }

    public static CategoryDto getDefaultCategoryDto() {
        return CategoryDto.builder()
                .name(NAME)
                .build();
    }
}
