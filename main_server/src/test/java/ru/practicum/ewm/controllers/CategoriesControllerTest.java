package ru.practicum.ewm.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.ewm.controllers.dtos.CategoryDto;
import ru.practicum.ewm.controllers.mappers.CategoryMapper;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.services.CategoryService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.ewm.utils.CategoriesTestUtils.CATEGORY_ID;
import static ru.practicum.ewm.utils.CategoriesTestUtils.generateCategories;
import static ru.practicum.ewm.utils.CategoriesTestUtils.getDefaultCategoryDto;
import static ru.practicum.ewm.utils.JsonTestUtils.configJsonProvider;
import static ru.practicum.ewm.exceptions.ErrorCode.BAD_REQUEST;
import static ru.practicum.ewm.exceptions.ErrorCode.CONFLICT;
import static ru.practicum.ewm.exceptions.ErrorCode.NOT_FOUND;


@WebMvcTest(controllers = CategoriesController.class)
@ExtendWith(MockitoExtension.class)
class CategoriesControllerTest {

    private static final String ADMIN_ENDPOINT = "/admin/categories";
    private static final String PUBLIC_ENDPOINT = "/categories";
    private static final int PAGE_START_FROM = 0;
    private static final int PAGE_SIZE = 10;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CategoryService categoryService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);

        configJsonProvider(mapper);
    }

    @Test
    void create_whenCategoryValid_return201() throws Exception {
        CategoryDto categoryDto = getDefaultCategoryDto();
        Category category = CategoryMapper.map(categoryDto);

        when(categoryService.create(any(Category.class))).thenReturn(category);

        MvcResult result = mockMvc.perform(post(ADMIN_ENDPOINT)
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.id"), is(category.getId()));
        assertThat(JsonPath.read(response, "$.name"), is(category.getName()));

        verify(categoryService, times(1))
                .create(any(Category.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void create_whenNameIsBlank_return400() throws Exception {
        CategoryDto categoryDto = getDefaultCategoryDto().toBuilder()
                .name("")
                .build();

        MvcResult result = mockMvc.perform(post(ADMIN_ENDPOINT)
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(BAD_REQUEST.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void create_whenNameIsNotUniq_return409() throws Exception {
        CategoryDto categoryDto = getDefaultCategoryDto();

        when(categoryService.create(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("test"));

        MvcResult result = mockMvc.perform(post(ADMIN_ENDPOINT)
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(CONFLICT.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verify(categoryService, times(1))
                .create(any(Category.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void get_whenCategoryExists_return200() throws Exception {
        CategoryDto categoryDto = getDefaultCategoryDto();
        Category category = CategoryMapper.map(categoryDto);

        when(categoryService.get(CATEGORY_ID)).thenReturn(category);

        MvcResult result = mockMvc.perform(get(PUBLIC_ENDPOINT + "/" + CATEGORY_ID))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.id"), is(category.getId()));
        assertThat(JsonPath.read(response, "$.name"), is(category.getName()));

        verify(categoryService, times(1))
                .get(CATEGORY_ID);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void create_whenCategoryNotFound_return404() throws Exception {
        when(categoryService.get(CATEGORY_ID)).thenThrow(new NotFoundException("test"));

        MvcResult result = mockMvc.perform(get(PUBLIC_ENDPOINT + "/" + CATEGORY_ID))
                .andExpect(status().isNotFound())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(NOT_FOUND.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verify(categoryService, times(1))
                .get(CATEGORY_ID);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void update_whenCategoryValid_return200() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .name("newName")
                .build();
        Category category = Category.builder()
                .id(CATEGORY_ID)
                .name("newName")
                .build();

        when(categoryService.update(any(Category.class))).thenReturn(category);

        MvcResult result = mockMvc.perform(patch(ADMIN_ENDPOINT + "/" + CATEGORY_ID)
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.id"), is(category.getId()));
        assertThat(JsonPath.read(response, "$.name"), is(category.getName()));

        verify(categoryService, times(1))
                .update(any(Category.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void update_whenNameIsBlank_return400() throws Exception {
        CategoryDto categoryDto = getDefaultCategoryDto().toBuilder()
                .name("")
                .build();

        MvcResult result = mockMvc.perform(patch(ADMIN_ENDPOINT + "/" + CATEGORY_ID)
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(BAD_REQUEST.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void update_whenCategoryNotFound_return404() throws Exception {
        CategoryDto categoryDto = getDefaultCategoryDto();

        when(categoryService.update(any(Category.class)))
                .thenThrow(new NotFoundException("test"));

        MvcResult result = mockMvc.perform(patch(ADMIN_ENDPOINT + "/" + CATEGORY_ID)
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(NOT_FOUND.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verify(categoryService, times(1))
                .update(any(Category.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void update_whenNameIsNotUniq_return409() throws Exception {
        CategoryDto categoryDto = getDefaultCategoryDto();

        when(categoryService.update(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("test"));

        MvcResult result = mockMvc.perform(patch(ADMIN_ENDPOINT + "/" + CATEGORY_ID)
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(CONFLICT.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verify(categoryService, times(1))
                .update(any(Category.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void delete_whenCategoryExists_return204() throws Exception {
        doNothing().when(categoryService).delete(CATEGORY_ID);

        MvcResult result = mockMvc.perform(delete(ADMIN_ENDPOINT + "/" + CATEGORY_ID))
                .andExpect(status().isNoContent())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response, is(""));

        verify(categoryService, times(1))
                .delete(CATEGORY_ID);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void delete_whenCategoryNotFound_return404() throws Exception {
        doThrow(new NotFoundException("test"))
                .when(categoryService).delete(CATEGORY_ID);

        MvcResult result = mockMvc.perform(delete(ADMIN_ENDPOINT + "/" + CATEGORY_ID))
                .andExpect(status().isNotFound())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(NOT_FOUND.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verify(categoryService, times(1))
                .delete(CATEGORY_ID);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void getAll() throws Exception {
        List<Category> categories = generateCategories(PAGE_SIZE);
        when(categoryService.getAll(PAGE_START_FROM, PAGE_SIZE))
                .thenReturn(new PageImpl<>(categories));

        mockMvc.perform(get(PUBLIC_ENDPOINT)
                        .param("from", String.valueOf(PAGE_START_FROM))
                        .param("size", String.valueOf(PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(categories.size())));

        verify(categoryService, times(1)).getAll(PAGE_START_FROM, PAGE_SIZE);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void getAll_whenCategoriesEmpty_returnEmptyList() throws Exception {
        List<Category> categories = new ArrayList<>();
        when(categoryService.getAll(PAGE_START_FROM, PAGE_SIZE))
                .thenReturn(new PageImpl<>(categories));

        mockMvc.perform(get(PUBLIC_ENDPOINT)
                        .param("from", String.valueOf(PAGE_START_FROM))
                        .param("size", String.valueOf(PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(categories.size())));

        verify(categoryService, times(1)).getAll(PAGE_START_FROM, PAGE_SIZE);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void getAll_whenParamsAreNotValid_return400() throws Exception {
        MvcResult result = mockMvc.perform(get(PUBLIC_ENDPOINT)
                        .param("from", "abc")
                        .param("size", "test"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(BAD_REQUEST.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verifyNoMoreInteractions(categoryService);
    }
}
