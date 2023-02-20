package ru.practicum.ewm.services;

import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;

public interface CategoryService {

    Category create(Category category);

    Category get(Long categoryId) throws NotFoundException;

    Category update(Category category);

    void delete(Long categoryId);
}
