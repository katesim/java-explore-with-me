package ru.practicum.ewm.hit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
}
