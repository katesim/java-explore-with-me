package ru.practicum.ewm.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventRequest;
import ru.practicum.ewm.entities.EventRequestState;
import ru.practicum.ewm.entities.EventStatus;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.EventRepository;
import ru.practicum.ewm.repositories.EventRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.practicum.ewm.common.EWMConstants.EVENT_NOT_FOUND_MSG_FORMAT;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {

    private static final String NOT_FOUND_MSG_FORMAT = "Event request with id=%d was not found";
    private static final String USER_REQUEST_FOR_ITS_OWN_EVENT_IS_REJECTED_ERROR_MSG = "Event initiator couldn't create request to participate in its own event";
    private static final String USER_REQUEST_FOR_UNPUBLISHED_EVENT_IS_REJECTED_ERROR_MSG = "Event must be published to participate";
    private static final String USER_REQUEST_FOR_NOT_AVAILABLE_EVENT_IS_REJECTED_ERROR_MSG = "Event must have free slots to participate";

    private final EventRequestRepository eventRequestRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventRequest create(final @NonNull EventRequest request) {
        final Event event = request.getEvent();

        if (!EventStatus.PUBLISHED.equals(event.getState())) {
            throw new ForbiddenOperation(USER_REQUEST_FOR_UNPUBLISHED_EVENT_IS_REJECTED_ERROR_MSG);
        } else if (event.getParticipantLimit()
                .equals(event.getConfirmedRequests())) {
            throw new ForbiddenOperation(USER_REQUEST_FOR_NOT_AVAILABLE_EVENT_IS_REJECTED_ERROR_MSG);
        }
        if (request.getEvent().getInitiator().getId()
                .equals(request.getRequester().getId())) {
            throw new ForbiddenOperation(USER_REQUEST_FOR_ITS_OWN_EVENT_IS_REJECTED_ERROR_MSG);
        }

        final boolean isAutoApproval = !event.getRequestModeration();
        final EventRequest.EventRequestBuilder newEventRequestBuilder = request.toBuilder();

        if (isAutoApproval) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);

            newEventRequestBuilder.status(EventRequestState.CONFIRMED);
        } else {
            newEventRequestBuilder.status(EventRequestState.PENDING);
        }

        newEventRequestBuilder.createdOn(LocalDateTime.now());

        return eventRequestRepository.save(newEventRequestBuilder.build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRequest> getAllByRequester(long requesterId) {
        return eventRequestRepository.findAllByRequesterId(requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRequest> getAllForEventIdByInitiator(List<Long> requestIds, long eventId, long initiatorId) {
        return eventRequestRepository.findAllWhereRequestIdInAndEventIdEqualsAndInitiatorIdEqualsAndStatusEquals(
                requestIds, eventId, initiatorId, null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EventRequest getById(long requestId) throws NotFoundException {
        Optional<EventRequest> eventRequest = eventRequestRepository.findById(requestId);
        if (eventRequest.isEmpty()) {
            final String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, requestId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return eventRequest.get();
    }

    @Override
    @Transactional
    public EventRequest cancelEventRequest(long requestId, long userId) throws NotFoundException {
        final EventRequest eventRequest = this.getById(requestId);
        if (!eventRequest.getRequester().getId()
                .equals(userId)) {
            final String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, requestId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        final EventRequest.EventRequestBuilder updatedEventRequestBuilder = eventRequest.toBuilder();
        updatedEventRequestBuilder.status(EventRequestState.CANCELED);
        return eventRequestRepository.save(updatedEventRequestBuilder.build());
    }

    @Override
    @Transactional
    public List<EventRequest> confirmEventRequests(
            @NonNull List<Long> requestIds,
            long eventId,
            long initiatorId
    ) throws NotFoundException, ForbiddenOperation {
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(EVENT_NOT_FOUND_MSG_FORMAT, eventId)));

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return this.getAllForEventIdByInitiator(
                    requestIds, eventId, initiatorId
            );
        }

        final List<EventRequest> eventRequests = this.getAllForEventIdByInitiator(requestIds, eventId, initiatorId);
        final List<EventRequest> confirmedEventRequests = new ArrayList<>();

        for (final EventRequest eventRequest : eventRequests) {
            if (!EventRequestState.PENDING.equals(eventRequest.getStatus())) {
                throw new ForbiddenOperation("Request should be in non-terminal state");
            }

            // increment event confirmed requests
            event.setConfirmedRequests(
                    event.getConfirmedRequests() + 1
            );

            // update eventRequest to Confirmed
            final EventRequest updatedEventRequest = eventRequest.toBuilder()
                    .status(EventRequestState.CONFIRMED)
                    .build();

            confirmedEventRequests.add(updatedEventRequest);
        }

        // if succeeds it means no db constraints are violated
        eventRepository.save(event);

        // save confirmed eventRequests
        eventRequestRepository.saveAll(confirmedEventRequests);

        if (!Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            return confirmedEventRequests;
        }

        // auto-reject all other requests from any user
        final List<EventRequest> rejectedEventRequests = new ArrayList<>();
        final List<EventRequest> pendingEventRequests = eventRequestRepository
                .findAllWhereRequestIdInAndEventIdEqualsAndInitiatorIdEqualsAndStatusEquals(
                        null, eventId, initiatorId, EventRequestState.PENDING
                );

        for (final EventRequest eventRequest : pendingEventRequests) {

            // update pending eventRequest to Rejected
            final EventRequest updatedEventRequest = eventRequest.toBuilder()
                    .status(EventRequestState.REJECTED)
                    .build();

            rejectedEventRequests.add(updatedEventRequest);
        }

        // save rejected eventRequests
        eventRequestRepository.saveAll(rejectedEventRequests);

        return confirmedEventRequests;
    }

    @Override
    @Transactional
    public List<EventRequest> rejectEventRequests(
            @NonNull List<Long> requestIds,
            long eventId,
            long initiatorId
    ) throws NotFoundException, ForbiddenOperation {
        final List<EventRequest> eventRequests = this.getAllForEventIdByInitiator(requestIds, eventId, initiatorId);
        final List<EventRequest> rejectedEventRequests = new ArrayList<>();

        for (final EventRequest eventRequest : eventRequests) {
            if (!EventRequestState.PENDING.equals(eventRequest.getStatus())) {
                throw new ForbiddenOperation("Request should be in non-terminal state");
            }

            // update eventRequest to Rejected
            final EventRequest updatedEventRequest = eventRequest.toBuilder()
                    .status(EventRequestState.REJECTED)
                    .build();

            rejectedEventRequests.add(updatedEventRequest);
        }

        // save rejected eventRequests
        eventRequestRepository.saveAll(rejectedEventRequests);

        return rejectedEventRequests;
    }
}
