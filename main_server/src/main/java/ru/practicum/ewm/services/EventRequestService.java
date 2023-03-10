package ru.practicum.ewm.services;

import lombok.NonNull;
import ru.practicum.ewm.entities.EventRequest;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;

public interface EventRequestService {

    EventRequest create(@NonNull final EventRequest request);

    List<EventRequest> getAllByRequester(long requesterId);

    List<EventRequest> getAllForEventIdByInitiator(
            List<Long> requestIds,
            long eventId,
            long initiatorId
    );

    EventRequest getById(long requestId) throws NotFoundException;

    EventRequest cancelEventRequest(long requestId, long userId) throws NotFoundException;

    List<EventRequest> confirmEventRequests(
            @NonNull List<Long> requestIds,
            long eventId,
            long userId
    ) throws NotFoundException, ForbiddenOperation;

    List<EventRequest> rejectEventRequests(
            @NonNull List<Long> requestIds,
            long eventId,
            long userId
    ) throws NotFoundException, ForbiddenOperation;
}
