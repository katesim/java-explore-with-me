package ru.practicum.ewm.hit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.ewm.dto.HitDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.ewm.hit.HitTestUtils.getDefaultHitDto;

@WebMvcTest(controllers = HitController.class)
@ExtendWith(MockitoExtension.class)
class HitControllerTest {

    private static final String ENDPOINT = "/hit";
    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HitService hitService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void create() throws Exception {
        HitDto hitDto = getDefaultHitDto();
        Hit hit = HitMapper.toHit(hitDto);

        when(hitService.add(any(Hit.class))).thenReturn(hit);

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .content(mapper.writeValueAsString(hitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(result.getResponse().getContentAsString(), is(""));
        verify(hitService, times(1))
                .add(any(Hit.class));
        verifyNoMoreInteractions(hitService);
    }

    @Test
    void create_whenTimestampIsInvalid_return400() throws Exception {
        HitDto hitDto = getDefaultHitDto().toBuilder()
                .timestamp("2022-09-06T11:00:23")
                .build();

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .content(mapper.writeValueAsString(hitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.code"), is(VALIDATION_ERROR_CODE));
        assertThat(JsonPath.read(response, "$.message"), notNullValue());

        verifyNoMoreInteractions(hitService);
    }

    @Test
    void create_whenUriIsBlank_return400() throws Exception {
        HitDto hitDto = getDefaultHitDto().toBuilder()
                .uri("")
                .build();

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .content(mapper.writeValueAsString(hitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(JsonPath.read(response, "$.code"), is(VALIDATION_ERROR_CODE));
        assertThat(JsonPath.read(response, "$.message"), notNullValue());

        verifyNoMoreInteractions(hitService);
    }
}
