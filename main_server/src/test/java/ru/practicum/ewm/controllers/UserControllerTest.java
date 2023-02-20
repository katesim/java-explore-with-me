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
import ru.practicum.ewm.controllers.dtos.UserDto;
import ru.practicum.ewm.controllers.mappers.UserMapper;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.services.UserService;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.ewm.utils.JsonTestUtils.configJsonProvider;
import static ru.practicum.ewm.utils.UserTestUtils.USER_ID;
import static ru.practicum.ewm.utils.UserTestUtils.generateUsers;
import static ru.practicum.ewm.utils.UserTestUtils.getDefaultUser;
import static ru.practicum.ewm.utils.UserTestUtils.getDefaultUserDto;
import static ru.practicum.ewm.exceptions.ErrorCode.BAD_REQUEST;
import static ru.practicum.ewm.exceptions.ErrorCode.CONFLICT;
import static ru.practicum.ewm.exceptions.ErrorCode.NOT_FOUND;

@WebMvcTest(controllers = UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String ADMIN_ENDPOINT = "/admin/users";
    private static final int PAGE_START_FROM = 0;
    private static final int PAGE_SIZE = 10;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);

        configJsonProvider(mapper);
    }

    @Test
    void create_whenUserValid_return201() throws Exception {
        UserDto userDto = getDefaultUserDto();
        User user = UserMapper.map(userDto);

        when(userService.create(any(User.class))).thenReturn(user);

        MvcResult result = mockMvc.perform(post(ADMIN_ENDPOINT)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.id"), is(user.getId()));
        assertThat(JsonPath.read(response, "$.name"), is(user.getName()));
        assertThat(JsonPath.read(response, "$.email"), is(user.getEmail()));

        verify(userService, times(1))
                .create(any(User.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void create_whenNameIsBlank_return400() throws Exception {
        UserDto userDto = getDefaultUserDto().toBuilder()
                .name("")
                .build();

        MvcResult result = mockMvc.perform(post(ADMIN_ENDPOINT)
                        .content(mapper.writeValueAsString(userDto))
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

        verifyNoMoreInteractions(userService);
    }

    @Test
    void create_whenEmailIsNotUniq_return409() throws Exception {
        UserDto userDto = getDefaultUserDto();

        when(userService.create(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("test"));

        MvcResult result = mockMvc.perform(post(ADMIN_ENDPOINT)
                        .content(mapper.writeValueAsString(userDto))
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

        verify(userService, times(1))
                .create(any(User.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void delete_whenUserExists_return204() throws Exception {
        doNothing().when(userService).delete(USER_ID);

        MvcResult result = mockMvc.perform(delete(ADMIN_ENDPOINT + "/" + USER_ID))
                .andExpect(status().isNoContent())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response, is(""));

        verify(userService, times(1))
                .delete(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void delete_whenUserNotFound_return404() throws Exception {
        doThrow(new NotFoundException("test"))
                .when(userService).delete(USER_ID);

        MvcResult result = mockMvc.perform(delete(ADMIN_ENDPOINT + "/" + USER_ID))
                .andExpect(status().isNotFound())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(NOT_FOUND.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verify(userService, times(1))
                .delete(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getAll() throws Exception {
        List<User> users = generateUsers(PAGE_SIZE);
        when(userService.getAll(PAGE_START_FROM, PAGE_SIZE))
                .thenReturn(new PageImpl<>(users));

        mockMvc.perform(get(ADMIN_ENDPOINT)
                        .param("from", String.valueOf(PAGE_START_FROM))
                        .param("size", String.valueOf(PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(users.size())));

        verify(userService, times(1)).getAll(PAGE_START_FROM, PAGE_SIZE);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getAll_whenFilterByIds_return200() throws Exception {
        User user = getDefaultUser().toBuilder()
                .id(2L)
                .build();
        List<User> users = List.of(user);
        List<Long> ids = List.of(2L);

        when(userService.getAllWithUserIds(ids, PAGE_START_FROM, PAGE_SIZE))
                .thenReturn(new PageImpl<>(users));

        mockMvc.perform(get(ADMIN_ENDPOINT)
                        .param("ids", "2")
                        .param("from", String.valueOf(PAGE_START_FROM))
                        .param("size", String.valueOf(PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(users.size())));

        verify(userService, times(1))
                .getAllWithUserIds(ids, PAGE_START_FROM, PAGE_SIZE);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getAll_whenUsersEmpty_returnEmptyList() throws Exception {
        List<User> categories = new ArrayList<>();
        when(userService.getAll(PAGE_START_FROM, PAGE_SIZE))
                .thenReturn(new PageImpl<>(categories));

        mockMvc.perform(get(ADMIN_ENDPOINT)
                        .param("from", String.valueOf(PAGE_START_FROM))
                        .param("size", String.valueOf(PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(categories.size())));

        verify(userService, times(1)).getAll(PAGE_START_FROM, PAGE_SIZE);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getAll_whenParamsAreNotValid_return400() throws Exception {
        MvcResult result = mockMvc.perform(get(ADMIN_ENDPOINT)
                        .param("from", "abc")
                        .param("size", "test"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.status"), is(BAD_REQUEST.name()));
        assertThat(JsonPath.read(response, "$.reason"), notNullValue());
        assertThat(JsonPath.read(response, "$.message"), notNullValue());
        assertThat(JsonPath.read(response, "$.timestamp"), notNullValue());

        verifyNoMoreInteractions(userService);
    }
}