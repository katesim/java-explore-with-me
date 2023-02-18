package ru.practicum.ewm.hit;

import java.time.LocalDateTime;

public class HitTestUtils {

    public static final String APP = "test-app";
    public static final String URI = "http://test-uri.com/123";
    public static final String IP_ADDRESS = "192.163.0.1";
    public static final String HIT_TIMESTAMP = "2022-09-06 11:00:23";
    public static final LocalDateTime HIT_DATETIME = LocalDateTime.of(2022, 9, 6, 11, 0, 23);

    public static Hit getDefaultHit() {
        return Hit.builder()
                .app(APP)
                .uri(URI)
                .ip(IP_ADDRESS)
                .timestamp(HIT_DATETIME)
                .build();
    }

    public static HitDto getDefaultHitDto() {
        return HitDto.builder()
                .app(APP)
                .uri(URI)
                .ip(IP_ADDRESS)
                .timestamp(HIT_TIMESTAMP)
                .build();
    }
}
