package ru.practicum.ewm.controllers.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.controllers.dtos.EventRequestDto;
import ru.practicum.ewm.entities.EventRequest;

import static ru.practicum.ewm.utils.DateTimeUtils.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventRequestMapper {

    public static EventRequestDto map(final EventRequest eventRequest) {
        return EventRequestDto.builder()
                .id(eventRequest.getId())
                .created(format(eventRequest.getCreatedOn()))
                .status(eventRequest.getStatus())
                .event(eventRequest.getEvent().getId())
                .requester(eventRequest.getRequester().getId())
                .build();
    }
}
