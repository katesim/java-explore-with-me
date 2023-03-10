package ru.practicum.ewm.services;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;

public interface CategoryService {

    Category create(Category category);

    Category get(long categoryId) throws NotFoundException;

    Page<Category> getAll(int from, int size);

    Category update(Category category);

    void delete(long categoryId) throws NotFoundException;
}
