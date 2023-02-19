package ru.practicum.ewm.hit;

import ru.practicum.ewm.dto.HitDto;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HitMapper {
    private static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);

    public static Hit toHit(final HitDto hitDto) {
        try {
            final LocalDateTime timestamp = LocalDateTime.parse(hitDto.getTimestamp(), DT_FORMATTER);

            return Hit.builder()
                    .app(hitDto.getApp())
                    .uri(hitDto.getUri())
                    .ip(hitDto.getIp())
                    .timestamp(timestamp)
                    .build();
        } catch (final DateTimeParseException exc) {
            throw new ValidationException(exc.getMessage(), exc);
        }
    }
}
