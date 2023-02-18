package ru.practicum.ewm.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;

    public List<HitCount> countHits(LocalDateTime start, LocalDateTime end) {
        return repository.countHits(start, end);
    }

    public List<HitCount> countHits(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return repository.countHits(start, end, uris);
    }

    public List<HitCount> countHitsUniq(LocalDateTime start, LocalDateTime end) {
        return repository.countHitsUniq(start, end);
    }

    public List<HitCount> countHitsUniq(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return repository.countHitsUniq(start, end, uris);
    }
}
