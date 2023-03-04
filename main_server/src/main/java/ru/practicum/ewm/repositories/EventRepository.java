package ru.practicum.ewm.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    String SEARCH_PUBLISHED_EVENTS_QUERY =
            "SELECT e " +
            "FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE e.state = 'PUBLISHED' " +
            "  AND (:text IS NULL " +
                    "OR (" +
                        "UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%')) " +
                        "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')) " +
                    ") " +
                ")" +
            "  AND (:paid IS NULL OR e.paid = :paid) " +
            "  AND (:onlyAvailable IS FALSE OR e.confirmedRequests < e.participantLimit) " +
            "  AND (:categories IS NULL OR c.id IN :categories) " +
            "  AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate >= :rangeStart) " +
            "  AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate <= :rangeEnd) ";

    String ORDER_BY_EVENT_DATE_ASC_CLAUSE = "ORDER BY e.eventDate ASC";

    Optional<Event> findByIdAndStateEquals(long eventId, final EventStatus state);

    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE u.id = ?1")
    Page<Event> findAllByInitiatorId(long userId, Pageable pageable);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE (:users IS NULL OR u.id IN :users) " +
            "  AND (:categories IS NULL OR c.id IN :categories) " +
            "  AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate >= :rangeStart) " +
            "  AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate <= :rangeEnd) " +
            "  AND (:states IS NULL OR e.state IN :states) "
    )
    Page<Event> findAllByInitiatorIdInAndCategoryIdInAndEventDateIsAfterAndEventDateIsBeforeAndStateIn(
            @Param("users") final List<Long> users,
            @Param("categories") final List<Long> categories,
            @Param("rangeStart") final LocalDateTime rangeStart,
            @Param("rangeEnd") final LocalDateTime rangeEnd,
            @Param("states") final List<EventStatus> states,
            Pageable pageable
    );

    @Query(SEARCH_PUBLISHED_EVENTS_QUERY + ORDER_BY_EVENT_DATE_ASC_CLAUSE)
    Page<Event> searchPublishedEventsOrderByEventDateAsc(
            @Param("text") final String text,
            @Param("paid") final Boolean paid,
            @Param("onlyAvailable") final Boolean onlyAvailable,
            @Param("categories") final List<Long> categories,
            @Param("rangeStart") final LocalDateTime rangeStart,
            @Param("rangeEnd") final LocalDateTime rangeEnd,
            Pageable pageable
    );
}
