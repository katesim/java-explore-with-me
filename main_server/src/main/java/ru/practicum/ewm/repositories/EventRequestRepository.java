package ru.practicum.ewm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.entities.EventRequest;
import ru.practicum.ewm.entities.EventRequestState;

import java.util.List;

public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {

    List<EventRequest> findAllByRequesterId(long requesterId);

    @Query("SELECT r " +
            "FROM EventRequest AS r " +
            "JOIN r.event AS e " +
            "JOIN e.initiator AS u " +
            "WHERE (:requestIds IS NULL OR r.id IN :requestIds) " +
            "  AND e.id = :eventId " +
            "  AND u.id = :initiatorId " +
            "  AND (:status IS NULL OR r.status = :status) "
    )
    List<EventRequest> findAllWhereRequestIdInAndEventIdEqualsAndInitiatorIdEqualsAndStatusEquals(
            @Param("requestIds") List<Long> requestIds,
            @Param("eventId") long eventId,
            @Param("initiatorId") long initiatorId,
            @Param("status") EventRequestState status
    );
}
