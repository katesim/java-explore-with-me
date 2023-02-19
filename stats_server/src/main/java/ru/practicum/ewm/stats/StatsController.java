package ru.practicum.ewm.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.StatsDto;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/stats")
@RequiredArgsConstructor
public class StatsController {

    private static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);

    private final StatsService service;

    @GetMapping
    public List<StatsDto> count(@RequestParam String start,
                                @RequestParam String end,
                                @RequestParam(required = false) List<String> uris,
                                @RequestParam(required = false) Boolean unique) {
        try {
            final LocalDateTime startDateTime = LocalDateTime.parse(start, DT_FORMATTER);
            final LocalDateTime endDateTime = LocalDateTime.parse(end, DT_FORMATTER);

            List<HitCount> result;
            boolean isUniq = unique != null && unique;

            if (isUniq) {
                result = uris == null
                        ? service.countHitsUniq(startDateTime, endDateTime)
                        : service.countHitsUniq(startDateTime, endDateTime, uris);
            } else {
                result = uris == null
                        ? service.countHits(startDateTime, endDateTime)
                        : service.countHits(startDateTime, endDateTime, uris);
            }

            return result.stream()
                    .map(StatsController::toStatsDto)
                    .collect(Collectors.toList());
        } catch (final DateTimeParseException exc) {
            throw new ValidationException(exc.getMessage(), exc);
        }
    }

    private static StatsDto toStatsDto(HitCount hitCount) {
        return StatsDto.builder()
                .app(hitCount.getApp())
                .uri(hitCount.getUri())
                .hits(hitCount.getHits())
                .build();
    }
}
