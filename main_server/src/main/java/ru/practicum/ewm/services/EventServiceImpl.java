package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventStatus;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.EventRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String NOT_FOUND_MSG_FORMAT = "Event with id=%d was not found or it's not owned by userId=%d";

    private final EventRepository repo;

    @Override
    @Transactional
    public Event create(Event event) {
        return repo.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> getAllByUserId(long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.findAllByInitiatorId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Event getByIdAndUserId(long eventId, long userId) throws NotFoundException {
        Optional<Event> event = repo.findByIdAndInitiatorId(eventId, userId);
        if (event.isEmpty()) {
            final String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, eventId, userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return event.get();
    }

    @Override
    @Transactional
    public Event updateByUserId(final Event updateEvent, long userId, long eventId)
            throws NotFoundException, ForbiddenOperation {
        final Event event = getByIdAndUserId(eventId, userId);

        if (event.getState() == EventStatus.PUBLISHED) {
            throw new ForbiddenOperation("Only pending or canceled events can be changed");
        }

        // Данные для изменения информации о событии. Если поле в запросе не указано (равно null)
        // - значит изменение этих данных не треубется.
        final Event.EventBuilder newEventBuilder = event.toBuilder();

        if (updateEvent.getTitle() != null) {
            newEventBuilder.title(updateEvent.getTitle());
        }
        if (updateEvent.getDescription() != null) {
            newEventBuilder.description(updateEvent.getDescription());
        }
        if (updateEvent.getAnnotation() != null) {
            newEventBuilder.annotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getEventDate() != null) {
            newEventBuilder.eventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLatitude() != null) {
            newEventBuilder.latitude(updateEvent.getLatitude());
        }
        if (updateEvent.getLongitude() != null) {
            newEventBuilder.longitude((updateEvent.getLongitude()));
        }
        if (updateEvent.getParticipantLimit() != null) {
            newEventBuilder.participantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getPaid() != null) {
            newEventBuilder.paid(updateEvent.getPaid());
        }
        if (updateEvent.getRequestModeration() != null) {
            newEventBuilder.requestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getCategory() != null) {
            newEventBuilder.category(updateEvent.getCategory());
        }
        if (updateEvent.getState() != null) {
            newEventBuilder.state(updateEvent.getState());
        }

        return repo.save(newEventBuilder.build());
    }
}
