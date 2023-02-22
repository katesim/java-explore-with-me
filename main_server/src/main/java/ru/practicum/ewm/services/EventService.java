package ru.practicum.ewm.services;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;

public interface EventService {

    Event create(Event event);

    Page<Event> getAllByUserId(long userId, int from, int size);

    Event getByIdAndUserId(long eventId, long userId) throws NotFoundException;

    Event updateByUserId(final Event updateEvent, long eventId, long userId)
            throws NotFoundException, ForbiddenOperation;
}
