package ru.practicum.ewm.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE u.id = ?1")
    Page<Event> findAllByInitiatorId(long userId, Pageable pageable);

    Page<Event> findAllByInitiatorIdInAndCategoryIdInAndEventDateIsAfterAndEventDateIsBeforeAndStateIn(
            final List<Long> users,
            final List<Long> categories,
            final LocalDateTime rangeStart,
            final LocalDateTime rangeEnd,
            final List<EventStatus> states,
            Pageable pageable
    );
}
