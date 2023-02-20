package ru.practicum.ewm.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.practicum.ewm.utils.CategoriesTestUtils.generateCategories;
import static ru.practicum.ewm.utils.CategoriesTestUtils.getDefaultCategory;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    private static final int PAGE_START_FROM = 0;
    private static final int PAGE_SIZE = 10;

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

    @Test
    void getAll() {
        List<Category> categories = generateCategories(PAGE_SIZE);
        Pageable pageable = PageRequest.of(PAGE_START_FROM, PAGE_SIZE);

        when(repository.findAll(eq(pageable)))
                .thenReturn(new PageImpl<>(categories));

        Page<Category> result = subject.getAll(PAGE_START_FROM, PAGE_SIZE);

        assertEquals(result.stream().collect(Collectors.toList()), categories);
        verify(repository, times(1)).findAll(any(Pageable.class));
    }
}
