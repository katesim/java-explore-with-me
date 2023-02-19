package ru.practicum.ewm.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.dto.HitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsClient {
    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";

    private static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);

    private final RestTemplate rest;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<Object> getStats(
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        return getStats(start, end, null, null);
    }

    public ResponseEntity<Object> getStats(
            final LocalDateTime start,
            final LocalDateTime end,
            @Nullable List<String> uris
    ) {
        return getStats(start, end, uris, null);
    }

    public ResponseEntity<Object> getStats(
            final LocalDateTime start,
            final LocalDateTime end,
            @Nullable Boolean unique
    ) {
        return getStats(start, end, null, unique);
    }

    public ResponseEntity<Object> getStats(
            final LocalDateTime start,
            final LocalDateTime end,
            @Nullable List<String> uris,
            @Nullable Boolean unique
    ) {
        Map<String, Object> parameters = new HashMap<>(Map.of(
                "start", start.format(DT_FORMATTER),
                "end", end.format(DT_FORMATTER)
        ));

        StringBuilder query = new StringBuilder("?start={start}&end={end}");

        if (uris != null) {
            parameters.put("uris", uris);
            query.append("&uris={uris}");
        }
        if (unique != null) {
            parameters.put("unique", uris);
            query.append("&unique={unique}");
        }

        return get(STATS_ENDPOINT + query, parameters);
    }

    public ResponseEntity<Object> recordHit(HitDto body) {
        return post(HIT_ENDPOINT, body, null);
    }

    private ResponseEntity<Object> get(String path, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, null, parameters);
    }

    private <T> ResponseEntity<Object> post(
            String path,
            T body,
            @Nullable Map<String, Object> parameters
    ) {
        return makeAndSendRequest(HttpMethod.POST, path, body, parameters);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(
            HttpMethod method,
            String path,
            @Nullable T body,
            @Nullable Map<String, Object> parameters
    ) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<Object> response;
        try {
            if (parameters != null) {
                response = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                response = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(response);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
