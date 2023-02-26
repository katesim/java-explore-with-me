package ru.practicum.ewm.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entities.Comment;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventStatus;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.ewm.common.EWMConstants.EVENT_NOT_FOUND_MSG_FORMAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String NOT_FOUND_FOR_USER_ERROR_MSG_FORMAT = "Event with id=%d was not found or it's not owned by userId=%d";
    private static final String USER_UPDATE_ON_PUBLISHED_EVENT_IS_REJECTED_ERROR_MSG = "Only pending or canceled events can be changed by user";
    private static final String ADMIN_PUBLISH_EVENT_IS_REJECTED_ERROR_MSG_FORMAT = "Cannot publish the event because it's not in the right state: %s";
    private static final String ADMIN_CANCEL_EVENT_IS_REJECTED_ERROR_MSG_FORMAT = "Cannot cancel the event because it's not in the right state: %s";
    private static final String INVALID_EVENT_DATE_ERROR_MSG_FORMAT = "eventDate должно содержать дату после: %s";
    private static final String COMMENT_TO_NON_PUBLISHED_ERROR_MSG_FORMAT = "коментировать можно только опубликованные события: %s";

    private static final int MINIMAL_EVENT_DATE_HOURS = 2;
    private static final int MINIMAL_PUBLISH_DATE_HOURS = 1;

    private final EventRepository repo;

    @Override
    @Transactional
    public Event create(final Event event) {
        final LocalDateTime now = LocalDateTime.now();

        if (event.getEventDate() != null) {
            validateEventDate(event.getEventDate(), now.plusHours(MINIMAL_EVENT_DATE_HOURS));
        }

        final Event newEvent = event.toBuilder()
                .createdOn(now)
                .state(EventStatus.PENDING)
                .confirmedRequests(0)
                .build();

        return repo.save(newEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> search(
            final List<Long> users,
            final List<EventStatus> states,
            final List<Long> categories,
            final LocalDateTime rangeStart,
            final LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.findAllByInitiatorIdInAndCategoryIdInAndEventDateIsAfterAndEventDateIsBeforeAndStateIn(
                users,
                categories,
                rangeStart,
                rangeEnd,
                states,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> searchPublishedEvents(
            final String text,
            final Boolean paid,
            final Boolean onlyAvailable,
            final List<Long> categories,
            final LocalDateTime rangeStart,
            final LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.searchPublishedEventsOrderByEventDateAsc(
                    text,
                    paid,
                    onlyAvailable,
                    categories,
                    rangeStart,
                    rangeEnd,
                    pageable
         );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllFilterByIds(@NonNull final List<Long> eventIds) {
        return repo.findAllById(eventIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> getAllByUserId(long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.findAllByInitiatorId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Event getById(long eventId, @NonNull final EventStatus state) throws NotFoundException {
        Optional<Event> event = repo.findByIdAndStateEquals(eventId, state);
        if (event.isEmpty()) {
            final String errorMessage = String.format(EVENT_NOT_FOUND_MSG_FORMAT, eventId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return event.get();
    }

    @Override
    @Transactional(readOnly = true)
    public Event getById(long eventId) throws NotFoundException {
        Optional<Event> event = repo.findById(eventId);
        if (event.isEmpty()) {
            final String errorMessage = String.format(EVENT_NOT_FOUND_MSG_FORMAT, eventId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return event.get();
    }

    @Override
    @Transactional
    public Event updateById(final Event updateEvent, long eventId)
            throws NotFoundException, ForbiddenOperation {

        final Event event = getById(eventId);
        final Event.EventBuilder updatedEventBuilder = event.toBuilder();

        final LocalDateTime now = LocalDateTime.now();

    if (updateEvent.getEventDate() != null) {
        validateEventDate(updateEvent.getEventDate(), now.plusHours(MINIMAL_EVENT_DATE_HOURS));
    }

        if (EventStatus.PUBLISHED.equals(updateEvent.getState())) {
            if (event.getState() != EventStatus.PENDING) {
                throw new ForbiddenOperation(
                        String.format(ADMIN_PUBLISH_EVENT_IS_REJECTED_ERROR_MSG_FORMAT, event.getState())
                );
            }
            final LocalDateTime eventDate = Optional.ofNullable(updateEvent.getEventDate())
                    .orElse(event.getEventDate());

            validateEventDate(eventDate, now.plusHours(MINIMAL_PUBLISH_DATE_HOURS));
            updatedEventBuilder.publishedOn(now);
        } else if (EventStatus.CANCELED.equals(updateEvent.getState())) {
            if (EventStatus.PUBLISHED.equals(event.getState())) {
                throw new ForbiddenOperation(
                        String.format(ADMIN_CANCEL_EVENT_IS_REJECTED_ERROR_MSG_FORMAT, event.getState())
                );
            }
        }

        final Event updatedEvent = this.updateEvent(event, updateEvent).build();
        return repo.save(updatedEvent);
    }

    @Override
    @Transactional
    public Event addComment(
            final Comment comment,
            long eventId
    ) throws NotFoundException, ForbiddenOperation {
        final Event event = getById(eventId);
        if (event.getState() != EventStatus.PUBLISHED) {
            throw new ForbiddenOperation(
                    String.format(COMMENT_TO_NON_PUBLISHED_ERROR_MSG_FORMAT, event.getState())
            );
        }

        event.addComment(comment);
        return repo.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Event getByIdAndUserId(long eventId, long userId) throws NotFoundException {
        Optional<Event> event = repo.findByIdAndInitiatorId(eventId, userId);
        if (event.isEmpty()) {
            final String errorMessage = String.format(NOT_FOUND_FOR_USER_ERROR_MSG_FORMAT, eventId, userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return event.get();
    }

    @Override
    @Transactional
    public Event updateByEventIdAndUserId(final Event updateEvent, long eventId, long userId)
            throws NotFoundException, ForbiddenOperation {
        final Event event = getByIdAndUserId(eventId, userId);

        final LocalDateTime now = LocalDateTime.now();

        if (updateEvent.getEventDate() != null) {
            validateEventDate(updateEvent.getEventDate(), now.plusHours(MINIMAL_EVENT_DATE_HOURS));
        }

        if (event.getState() == EventStatus.PUBLISHED) {
            throw new ForbiddenOperation(USER_UPDATE_ON_PUBLISHED_EVENT_IS_REJECTED_ERROR_MSG);
        }

        final Event updatedEvent = this.updateEvent(event, updateEvent).build();
        return repo.save(updatedEvent);
    }

    private Event.EventBuilder updateEvent(final Event event, final Event updateEvent) {
        final Event.EventBuilder updatedEventBuilder = event.toBuilder();

        // Если поле не указано (равно null), или совпадает с текущим значением
        // – значит изменение этого поля не треубется.

        if (updateEvent.getTitle() != null
                && !event.getTitle().equals(updateEvent.getTitle())) {
            updatedEventBuilder.title(updateEvent.getTitle());
        }
        if (updateEvent.getDescription() != null
                && !event.getDescription().equals(updateEvent.getDescription())) {
            updatedEventBuilder.description(updateEvent.getDescription());
        }
        if (updateEvent.getAnnotation() != null
                && !event.getAnnotation().equals(updateEvent.getAnnotation())) {
            updatedEventBuilder.annotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getEventDate() != null
                && !event.getEventDate().equals(updateEvent.getEventDate())) {
            updatedEventBuilder.eventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLatitude() != null
                && !event.getLatitude().equals(updateEvent.getLatitude())) {
            updatedEventBuilder.latitude(updateEvent.getLatitude());
        }
        if (updateEvent.getLongitude() != null
                && !event.getLongitude().equals(updateEvent.getLongitude())) {
            updatedEventBuilder.longitude(updateEvent.getLongitude());
        }
        if (updateEvent.getParticipantLimit() != null
                && !event.getParticipantLimit().equals(updateEvent.getParticipantLimit())) {
            updatedEventBuilder.participantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getPaid() != null
                && !event.getPaid().equals(updateEvent.getPaid())) {
            updatedEventBuilder.paid(updateEvent.getPaid());
        }
        if (updateEvent.getRequestModeration() != null
                && !event.getRequestModeration().equals(updateEvent.getRequestModeration())) {
            updatedEventBuilder.requestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getCategory() != null
                && !event.getCategory().equals(updateEvent.getCategory())) {
            updatedEventBuilder.category(updateEvent.getCategory());
        }
        if (updateEvent.getState() != null
                && !event.getState().equals(updateEvent.getState())) {
            updatedEventBuilder.state(updateEvent.getState());
        }

        return updatedEventBuilder;
    }

    private void validateEventDate(
            @NonNull final LocalDateTime eventDate,
            @NonNull final LocalDateTime minimalEventDate
    ) throws ForbiddenOperation {
        if (eventDate.isBefore(minimalEventDate)) {
            throw new ForbiddenOperation(String.format(INVALID_EVENT_DATE_ERROR_MSG_FORMAT, minimalEventDate));
        }
    }
}
