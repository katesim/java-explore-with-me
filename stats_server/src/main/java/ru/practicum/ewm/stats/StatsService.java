package ru.practicum.ewm.stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    List<HitCount> countHits(LocalDateTime start, LocalDateTime end);

    List<HitCount> countHits(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<HitCount> countHitsUniq(LocalDateTime start, LocalDateTime end);

    List<HitCount> countHitsUniq(LocalDateTime start, LocalDateTime end, List<String> uris);
}
