package ru.practicum.ewm.controllers.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.controllers.dtos.CategoryDto;
import ru.practicum.ewm.controllers.dtos.CreateEventRequestDto;
import ru.practicum.ewm.controllers.dtos.EventDto;
import ru.practicum.ewm.controllers.dtos.LocationDto;
import ru.practicum.ewm.controllers.dtos.UpdateEventUserRequestDto;
import ru.practicum.ewm.controllers.dtos.UserShortDto;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.entities.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    private static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);

    public static Event map(final CreateEventRequestDto eventDto) {
        final LocalDateTime eventDate;

        try {
            eventDate = LocalDateTime.parse(eventDto.getEventDate(), DT_FORMATTER);
        } catch (final DateTimeParseException exc) {
            throw new ValidationException(exc.getMessage(), exc);
        }

        return Event.builder()
                .title(eventDto.getTitle())
                .description(eventDto.getDescription())
                .annotation(eventDto.getAnnotation())
                .eventDate(eventDate)
                .latitude(eventDto.getLocation().getLat())
                .longitude(eventDto.getLocation().getLon())
                .participantLimit(eventDto.getParticipantLimit())
                .paid(eventDto.getPaid())
                .requestModeration(eventDto.getRequestModeration())
                .build();
    }

    public static Event map(final UpdateEventUserRequestDto eventDto) {
        LocalDateTime eventDate = null;

        if (eventDto.getEventDate() != null) {
            try {
                eventDate = LocalDateTime.parse(eventDto.getEventDate(), DT_FORMATTER);
            } catch (final DateTimeParseException exc) {
                throw new ValidationException(exc.getMessage(), exc);
            }
        }

        final Optional<LocationDto> locationDto = Optional.ofNullable(eventDto.getLocation());

        return Event.builder()
                .title(eventDto.getTitle())
                .description(eventDto.getDescription())
                .annotation(eventDto.getAnnotation())
                .eventDate(eventDate)
                .latitude(locationDto.map(LocationDto::getLat).orElse(null))
                .longitude(locationDto.map(LocationDto::getLon).orElse(null))
                .participantLimit(eventDto.getParticipantLimit())
                .paid(eventDto.getPaid())
                .requestModeration(eventDto.getRequestModeration())
                .build();
    }

    public static EventDto map(final Event event) {
        final User initiator = event.getInitiator();
        final UserShortDto initiatorDto = UserShortDto.builder()
                .id(initiator.getId())
                .name(initiator.getName())
                .build();

        final Category category = event.getCategory();
        final CategoryDto categoryDto = CategoryMapper.map(category);

        final LocationDto locationDto = LocationDto.builder()
                .lat(event.getLatitude())
                .lon(event.getLongitude())
                .build();

        return EventDto.builder()
                .id(event.getId())
                .state(event.getState())
                .initiator(initiatorDto)
                .category(categoryDto)
                .createdOn(event.getCreatedOn().format(DT_FORMATTER))
                .publishedOn(
                        Optional.ofNullable(event.getPublishedOn())
                                .map((x) -> x.format(DT_FORMATTER))
                                .orElse(null))
                .title(event.getTitle())
                .description(event.getDescription())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate().format(DT_FORMATTER))
                .location(locationDto)
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .paid(event.getPaid())
                .requestModeration(event.getRequestModeration())
                .build();
    }
}
