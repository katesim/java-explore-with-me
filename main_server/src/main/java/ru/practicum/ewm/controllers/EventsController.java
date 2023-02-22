package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.controllers.dtos.UpdateEventStateUserAction;
import ru.practicum.ewm.controllers.dtos.CreateEventRequestDto;
import ru.practicum.ewm.controllers.dtos.EventDto;
import ru.practicum.ewm.controllers.dtos.UpdateEventUserRequestDto;
import ru.practicum.ewm.controllers.mappers.EventMapper;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventStatus;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.markers.Create;
import ru.practicum.ewm.markers.Update;
import ru.practicum.ewm.services.CategoryService;
import ru.practicum.ewm.services.EventService;
import ru.practicum.ewm.services.UserService;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.common.EWMConstants.PAGE_SIZE_DEFAULT_TEXT;
import static ru.practicum.ewm.common.EWMConstants.PAGE_START_FROM_DEFAULT_TEXT;
import static ru.practicum.ewm.controllers.mappers.EventMapper.map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class EventsController {

    private static final String EVENT_DATE_ERROR_MSG = "Error: eventDate должно содержать дату после: %s";

    private final UserService userService;
    private final EventService eventService;
    private final CategoryService categoryService;

    // Private

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(
            @PathVariable long userId,
            @Validated(Create.class) @RequestBody CreateEventRequestDto eventDto
    ) {
        Event event = map(eventDto);

        final LocalDateTime now = LocalDateTime.now();
        validateEventDate(event.getEventDate(), now.plusHours(2));

        final User user = userService.get(userId);
        final Category category = categoryService.get(eventDto.getCategory());

        event = event.toBuilder()
                .initiator(user)
                .category(category)
                .createdOn(now)
                .state(EventStatus.PENDING)
                .confirmedRequests(0)
                .build();

        log.info("Event: {}", event);
        return map(eventService.create(event));
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getByIdAndUserId(@PathVariable long userId, @PathVariable long eventId) {
        final Event event = eventService.getByIdAndUserId(eventId, userId);
        return map(event);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getAllByUserId(
            @PathVariable long userId,
            @RequestParam(defaultValue = PAGE_START_FROM_DEFAULT_TEXT, required = false) @Min(0) int from,
            @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_TEXT, required = false) @Min(1) int size) {
        return eventService.getAllByUserId(userId, from, size).stream()
                .map(EventMapper::map)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto update(
            @PathVariable long userId,
            @PathVariable long eventId,
            @Validated(Update.class) @RequestBody UpdateEventUserRequestDto eventDto
    ) {
        Event updateEvent = map(eventDto);

        if (eventDto.getEventDate() != null) {
            final LocalDateTime now = LocalDateTime.now();
            validateEventDate(updateEvent.getEventDate(), now.plusHours(2));
        }

        final Event.EventBuilder eventBuilder = updateEvent.toBuilder();

        if (eventDto.getCategory() != null) {
            final Category category = categoryService.get(eventDto.getCategory());
            eventBuilder.category(category);
        }
        if (eventDto.getStateAction() != null) {
            final EventStatus updateState =
                    eventDto.getStateAction() == UpdateEventStateUserAction.SEND_TO_REVIEW
                            ? EventStatus.PENDING
                            : EventStatus.CANCELED;
            eventBuilder.state(updateState);
        }

        updateEvent = eventBuilder.build();
        final Event savedEvent = eventService.updateByUserId(updateEvent, eventId, userId);
        return map(savedEvent);
    }

    private void validateEventDate(final LocalDateTime eventDate, final LocalDateTime minimalEventDate)
            throws ForbiddenOperation {

        if (eventDate.isBefore(minimalEventDate)) {
            throw new ForbiddenOperation(String.format(EVENT_DATE_ERROR_MSG, minimalEventDate));
        }
    }
}
