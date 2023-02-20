package ru.practicum.ewm.controllers.categories;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.services.CategoryService;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.ewm.controllers.categories.CategoriesTestUtils.getDefaultCategoryDto;
import static ru.practicum.ewm.controllers.utils.JsonTestUtils.configJsonProvider;
import static ru.practicum.ewm.exceptions.ErrorCode.BAD_REQUEST;
import static ru.practicum.ewm.exceptions.ErrorCode.CONFLICT;
import static ru.practicum.ewm.exceptions.ErrorCode.NOT_FOUND;


@WebMvcTest(controllers = CategoriesController.class)
@ExtendWith(MockitoExtension.class)
class CategoriesControllerTest {

    private static final String ADMIN_ENDPOINT = "/admin/categories";
    private static final Long CATEGORY_ID = 123L;

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
    void update_whenCategoryValid_return200() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .name("newName")
                .build();
        Category category = Category.builder()
                .id(123L)
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
}