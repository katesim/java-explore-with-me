package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.controllers.dtos.EventRequestDto;
import ru.practicum.ewm.controllers.dtos.EventRequestStatusUpdateRequestDto;
import ru.practicum.ewm.controllers.dtos.EventRequestStatusUpdateResponseDto;
import ru.practicum.ewm.controllers.mappers.EventRequestMapper;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventRequest;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.services.EventRequestService;
import ru.practicum.ewm.services.EventService;
import ru.practicum.ewm.services.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.controllers.mappers.EventRequestMapper.map;

@RestController
@RequiredArgsConstructor
public class EventRequestsController {

    private static final String EVENT_REQUESTS_ENDPOINT_PREFIX = "/users/{userId}/requests";
    private static final String USER_EVENT_REQUESTS_ENDPOINT_PREFIX = "/users/{userId}/events/{eventId}/requests";

    private final UserService userService;
    private final EventService eventService;
    private final EventRequestService eventRequestService;

    // Private

    @GetMapping(EVENT_REQUESTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<EventRequestDto> getAllByRequester(@PathVariable long userId) {
        // check if user exists
        userService.get(userId);
        return eventRequestService.getAllByRequester(userId).stream()
                .map(EventRequestMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping(USER_EVENT_REQUESTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<EventRequestDto> getAllByInitiator(
            @PathVariable long userId,
            @PathVariable long eventId
    ) {
        // check if user exists
        userService.get(userId);
        return eventRequestService.getAllForEventIdByInitiator(null, eventId, userId).stream()
                .map(EventRequestMapper::map)
                .collect(Collectors.toList());
    }

    @PostMapping(EVENT_REQUESTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.CREATED)
    public EventRequestDto create(
            @PathVariable long userId, @RequestParam int eventId
    ) {
        final Event event = eventService.getById(eventId);
        final User requester = userService.get(userId);

        final EventRequest eventRequest = EventRequest.builder()
                .createdOn(LocalDateTime.now())
                .requester(requester)
                .event(event)
                .build();
        return map(eventRequestService.create(eventRequest));
    }

    @PatchMapping(EVENT_REQUESTS_ENDPOINT_PREFIX + "/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestDto cancelEventRequest(
            @PathVariable long userId,
            @PathVariable long requestId
    ) {
        return map(eventRequestService.cancelEventRequest(requestId, userId));
    }

    @PatchMapping(USER_EVENT_REQUESTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResponseDto statusUpdate(
            @PathVariable long userId,
            @PathVariable long eventId,
            @RequestBody EventRequestStatusUpdateRequestDto eventRequestStatusUpdateRequestDto
    ) {
        final List<Long> requestIds = eventRequestStatusUpdateRequestDto.getRequestIds();

        final EventRequestStatusUpdateResponseDto.EventRequestStatusUpdateResponseDtoBuilder<?, ?> responseBuilder
                = EventRequestStatusUpdateResponseDto.builder();

        final EventRequestStatusUpdateRequestDto.EventRequestStatusUpdateAction statusUpdateAction
                = eventRequestStatusUpdateRequestDto.getStatus();

        if (EventRequestStatusUpdateRequestDto.EventRequestStatusUpdateAction.CONFIRMED
                .equals(statusUpdateAction)
        ) {
            final List<EventRequestDto> confirmedEventRequests = eventRequestService.confirmEventRequests(
                            requestIds,
                            eventId,
                            userId
                    )
                    .stream()
                    .map(EventRequestMapper::map)
                    .collect(Collectors.toList());

            responseBuilder.confirmedRequests(confirmedEventRequests);
        } else if (EventRequestStatusUpdateRequestDto.EventRequestStatusUpdateAction.REJECTED
                .equals(statusUpdateAction)
        ) {
            final List<EventRequestDto> rejectedEventRequests = eventRequestService.rejectEventRequests(
                            requestIds,
                            eventId,
                            userId
                    )
                    .stream()
                    .map(EventRequestMapper::map)
                    .collect(Collectors.toList());

            responseBuilder.rejectedRequests(rejectedEventRequests);
        } else {
            throw new ValidationException(
                    String.format("Unexpected status: %s", statusUpdateAction)
            );
        }

        return responseBuilder.build();
    }
}
