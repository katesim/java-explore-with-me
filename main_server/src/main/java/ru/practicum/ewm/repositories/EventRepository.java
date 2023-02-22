package ru.practicum.ewm.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.entities.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE u.id = ?1")
    Page<Event> findAllByInitiatorId(long userId, Pageable pageable);
}
