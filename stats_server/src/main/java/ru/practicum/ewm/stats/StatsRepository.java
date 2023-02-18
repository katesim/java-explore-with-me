package ru.practicum.ewm.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.hit.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(*) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN ?1 AND ?2) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<HitCount> countHits(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(*) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN ?1 AND ?2) AND h.uri in ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<HitCount> countHits(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN ?1 AND ?2) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<HitCount> countHitsUniq(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN ?1 AND ?2) AND h.uri in ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<HitCount> countHitsUniq(LocalDateTime start, LocalDateTime end, List<String> uris);
}
