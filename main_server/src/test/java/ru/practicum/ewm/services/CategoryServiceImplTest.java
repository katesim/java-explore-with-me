package ru.practicum.ewm.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.CategoryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.practicum.ewm.controllers.categories.CategoriesTestUtils.getDefaultCategory;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryServiceImpl subject;

    @Test
    void create() {
        Category category = getDefaultCategory();

        when(repository.save(category)).thenReturn(category);

        Category result = subject.create(category);

        assertEquals(result, category);
        verify(repository, times(1)).save(category);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void get_whenCategoryExists_returnCategory() {
        Category category = getDefaultCategory();

        when(repository.findById(category.getId())).thenReturn(Optional.of(category));

        Category result = subject.get(category.getId());

        assertEquals(result, category);
        verify(repository, times(1)).findById(category.getId());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void get_whenCategoryNotExists_throwNotFound() {
        Category category = getDefaultCategory();

        when(repository.findById(category.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> subject.get(category.getId()));
        verify(repository, times(1)).findById(category.getId());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void update() {
        Category category = getDefaultCategory();

        when(repository.save(category)).thenReturn(category);

        Category result = subject.update(category);

        assertEquals(result, category);
        verify(repository, times(1)).save(category);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void delete_whenCategoryExists_deleteCategory() {
        Category category = getDefaultCategory();

        when(repository.findById(category.getId())).thenReturn(Optional.of(category));
        doNothing().when(repository).deleteById(category.getId());

        subject.delete(category.getId());

        verify(repository, times(1)).findById(category.getId());
        verify(repository, times(1)).deleteById(category.getId());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void delete_whenCategoryNotExists_throwNotFound() {
        Category category = getDefaultCategory();

        when(repository.findById(category.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> subject.delete(category.getId()));
        verify(repository, times(1)).findById(category.getId());
        verifyNoMoreInteractions(repository);
    }
}