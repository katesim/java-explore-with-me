package ru.practicum.ewm.utils;

import ru.practicum.ewm.controllers.dtos.CategoryDto;
import ru.practicum.ewm.entities.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesTestUtils {

    public static final Long CATEGORY_ID = 123L;
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

    public static List<Category> generateCategories(final int count) {
        List<Category> categories = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            final Category category = Category.builder()
                    .id(i)
                    .name(NAME + i)
                    .build();
            categories.add(category);
        }

        return categories;
    }
}
