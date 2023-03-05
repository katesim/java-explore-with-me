package ru.practicum.ewm.services;


import ru.practicum.ewm.dto.StatsDto;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface HitService {

    void recordHit(@NotNull String uri, @NotNull String ip);

    Map<String, StatsDto> getStats(List<String> uris);
}
