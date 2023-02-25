package ru.practicum.ewm.controllers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.controllers.dtos.UpdateEventStateAction;
import ru.practicum.ewm.controllers.dtos.CreateEventRequestDto;
import ru.practicum.ewm.controllers.dtos.EventDto;
import ru.practicum.ewm.controllers.dtos.UpdateEventRequestDto;
import ru.practicum.ewm.controllers.mappers.EventMapper;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.EventStatus;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.markers.Create;
import ru.practicum.ewm.markers.Update;
import ru.practicum.ewm.services.CategoryService;
import ru.practicum.ewm.services.EventService;
import ru.practicum.ewm.services.UserService;
import ru.practicum.ewm.utils.DateTimeUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.ewm.common.EWMConstants.PAGE_SIZE_DEFAULT_TEXT;
import static ru.practicum.ewm.common.EWMConstants.PAGE_START_FROM_DEFAULT_TEXT;
import static ru.practicum.ewm.controllers.mappers.EventMapper.map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventsController {

    private static final String USER_EVENTS_ENDPOINT_PREFIX = "/users/{userId}/events";
    private static final String ADMIN_EVENTS_ENDPOINT_PREFIX = "/admin/events";

    private static final Set<UpdateEventStateAction> USER_ALLOWED_UPDATE_EVENT_STATE_ACTIONS
            = Set.of(UpdateEventStateAction.SEND_TO_REVIEW, UpdateEventStateAction.CANCEL_REVIEW);
    private static final Set<UpdateEventStateAction> ADMIN_ALLOWED_UPDATE_EVENT_STATE_ACTIONS
            = Set.of(UpdateEventStateAction.PUBLISH_EVENT, UpdateEventStateAction.REJECT_EVENT);

    private final UserService userService;
    private final EventService eventService;
    private final CategoryService categoryService;

    // Private

    @PostMapping(USER_EVENTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(
            @PathVariable long userId,
            @Validated(Create.class) @RequestBody CreateEventRequestDto eventDto
    ) {
        Event event = map(eventDto);

        final LocalDateTime now = LocalDateTime.now();
        final User user = userService.get(userId);
        final Category category = categoryService.get(eventDto.getCategory());

        event = event.toBuilder()
                .initiator(user)
                .category(category)
                .createdOn(now)
                .state(EventStatus.PENDING)
                .confirmedRequests(0)
                .build();

        log.info("Create event: {}", event);
        return map(eventService.create(event));
    }

    @GetMapping(USER_EVENTS_ENDPOINT_PREFIX + "/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getByIdAndUserId(@PathVariable long userId, @PathVariable long eventId) {
        final Event event = eventService.getByIdAndUserId(eventId, userId);
        return map(event);
    }

    @GetMapping(USER_EVENTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getAllByUserId(
            @PathVariable long userId,
            @RequestParam(defaultValue = PAGE_START_FROM_DEFAULT_TEXT, required = false) @Min(0) int from,
            @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_TEXT, required = false) @Min(1) int size) {
        return eventService.getAllByUserId(userId, from, size).stream()
                .map(EventMapper::map)
                .collect(Collectors.toList());
    }

    @PatchMapping(USER_EVENTS_ENDPOINT_PREFIX + "/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto update(
            @PathVariable long userId,
            @PathVariable long eventId,
            @Validated(Update.class) @RequestBody UpdateEventRequestDto eventDto
    ) {
        final Event.EventBuilder updateEventBuilder = map(eventDto).toBuilder();

        if (eventDto.getStateAction() != null) {
            validateEventStateUpdateUserAction(eventDto.getStateAction());
            final EventStatus updateState =
                    eventDto.getStateAction() == UpdateEventStateAction.SEND_TO_REVIEW
                            ? EventStatus.PENDING
                            : EventStatus.CANCELED;
            updateEventBuilder.state(updateState);
        }
        if (eventDto.getCategory() != null) {
            final Category category = categoryService.get(eventDto.getCategory());
            updateEventBuilder.category(category);
        }

        final Event updateEvent = updateEventBuilder.build();
        log.info("Update event: {}", updateEvent);
        final Event savedEvent = eventService.updateByEventIdAndUserId(updateEvent, eventId, userId);
        log.info("Saved event: {}", savedEvent);
        return map(savedEvent);
    }

    // Admin

    @GetMapping(ADMIN_EVENTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> search(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = PAGE_START_FROM_DEFAULT_TEXT, required = false) @Min(0) int from,
            @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_TEXT, required = false) @Min(1) int size
    ) {
        final List<EventStatus> mappedStates = Optional.ofNullable(states).map(
                        list -> list.stream()
                                .filter(Objects::nonNull)
                                .map(EventStatus::valueOf)
                                .collect(Collectors.toList())
                ).orElse(null);

        final LocalDateTime eventDateStart = Optional.ofNullable(rangeStart)
                .map(DateTimeUtils::parse)
                .orElse(null);
        final LocalDateTime eventDateEnd = Optional.ofNullable(rangeEnd)
                .map(DateTimeUtils::parse)
                .orElse(null);

        return eventService.search(users, mappedStates, categories, eventDateStart, eventDateEnd, from, size)
                .stream()
                .map(EventMapper::map)
                .collect(Collectors.toList());
    }

    @PatchMapping(ADMIN_EVENTS_ENDPOINT_PREFIX + "/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto update(
            @PathVariable long eventId,
            @Validated(Update.class) @RequestBody UpdateEventRequestDto eventDto
    ) {
        final Event.EventBuilder updateEventBuilder = map(eventDto).toBuilder();

        if (eventDto.getStateAction() != null) {
            validateEventStateUpdateAdminAction(eventDto.getStateAction());
            final EventStatus updateState =
                    eventDto.getStateAction() == UpdateEventStateAction.PUBLISH_EVENT
                            ? EventStatus.PUBLISHED
                            : EventStatus.CANCELED;
            updateEventBuilder.state(updateState);
        }
        if (eventDto.getCategory() != null) {
            final Category category = categoryService.get(eventDto.getCategory());
            updateEventBuilder.category(category);
        }

        final Event updateEvent = updateEventBuilder.build();
        log.info("Update event: {}", updateEvent);
        final Event savedEvent = eventService.updateById(updateEventBuilder.build(), eventId);
        log.info("Saved event: {}", savedEvent);
        return map(savedEvent);
    }

    private void validateEventStateUpdateUserAction(
            @NonNull final UpdateEventStateAction stateAction
    ) throws ValidationException {
        if (!USER_ALLOWED_UPDATE_EVENT_STATE_ACTIONS.contains(stateAction)) {
            throw new ValidationException("Not allowed state update for User");
        }
    }

    private void validateEventStateUpdateAdminAction(
            @NonNull final UpdateEventStateAction stateAction
    ) throws ValidationException {
        if (!ADMIN_ALLOWED_UPDATE_EVENT_STATE_ACTIONS.contains(stateAction)) {
            throw new ValidationException("Not allowed state update for Admin");
        }
    }
}
