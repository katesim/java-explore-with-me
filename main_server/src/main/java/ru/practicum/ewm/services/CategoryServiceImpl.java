package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.CategoryRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String NOT_FOUND_MSG_FORMAT = "Category with id=%d was not found";

    private final CategoryRepository repo;

    @Override
    @Transactional
    public Category create(Category category) {
        return repo.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category get(Long categoryId) throws NotFoundException {
        Optional<Category> category = repo.findById(categoryId);
        if (category.isEmpty()) {
            final String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, categoryId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return category.get();
    }

    @Override
    @Transactional
    public Category update(Category category) {
        return repo.save(category);
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        this.get(categoryId);
        repo.deleteById(categoryId);
    }
}
