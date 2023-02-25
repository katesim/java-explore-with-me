package ru.practicum.ewm.services;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import ru.practicum.ewm.controllers.dtos.SortType;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventStatus;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    Event create(Event event);

    Page<Event> search(
            final List<Long> users,
            final List<EventStatus> states,
            final List<Long> categories,
            final LocalDateTime rangeStart,
            final LocalDateTime rangeEnd,
            int from,
            int size
    );

    Page<Event> searchPublishedEvents(
            final String text,
            final Boolean paid,
            final Boolean onlyAvailable,
            final List<Long> categories,
            final LocalDateTime rangeStart,
            final LocalDateTime rangeEnd,
            final SortType sort,
            int from,
            int size
    );

    Page<Event> getAllByUserId(long userId, int from, int size);

    Event getById(long eventId) throws NotFoundException;

    Event getById(long eventId, @NonNull final EventStatus state) throws NotFoundException;

    Event updateById(final Event updateEvent, long eventId)
            throws NotFoundException, ForbiddenOperation;

    Event getByIdAndUserId(long eventId, long userId) throws NotFoundException;

    Event updateByEventIdAndUserId(final Event updateEvent, long eventId, long userId)
            throws NotFoundException, ForbiddenOperation;
}
