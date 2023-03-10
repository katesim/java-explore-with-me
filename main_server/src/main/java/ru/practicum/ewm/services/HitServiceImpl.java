package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.HitDto;
import ru.practicum.ewm.dto.StatsDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.ewm.common.EWMConstants.APP_NAME;
import static ru.practicum.ewm.utils.DateTimeUtils.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private static final LocalDateTime UNIX_EPOCH = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

    private final StatsClient client;

    @Override
    public void recordHit(@NotNull String uri, @NotNull String ip) {
        final LocalDateTime now = LocalDateTime.now();
        final HitDto hit = HitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(format(now))
                .build();

        client.recordHit(hit);
    }

    @Override
    public Map<String, StatsDto> getStats(List<String> uris) {
        final LocalDateTime now = LocalDateTime.now();
        final List<LinkedHashMap> stats =  client.getStats(UNIX_EPOCH, now, uris);

        final Map<String, StatsDto> statsByUri = new HashMap<>();
        for (final LinkedHashMap stat : stats) {
            final StatsDto statsDto = StatsDto.builder()
                    .app(stat.get("app").toString())
                    .uri(stat.get("uri").toString())
                    .hits(Long.parseLong(stat.get("hits").toString()))
                    .build();

            statsByUri.put(statsDto.getUri(), statsDto);
        }

        return statsByUri;
    }
}
