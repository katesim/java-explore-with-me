package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.controllers.dtos.CompilationRequestDto;
import ru.practicum.ewm.controllers.dtos.CompilationResponseDto;
import ru.practicum.ewm.controllers.mappers.CompilationMapper;
import ru.practicum.ewm.entities.Compilation;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.markers.Create;
import ru.practicum.ewm.markers.Update;
import ru.practicum.ewm.services.CompilationService;
import ru.practicum.ewm.services.EventService;

import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.common.EWMConstants.PAGE_SIZE_DEFAULT_TEXT;
import static ru.practicum.ewm.common.EWMConstants.PAGE_START_FROM_DEFAULT_TEXT;
import static ru.practicum.ewm.controllers.mappers.CompilationMapper.map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CompilationController {

    private static final String ADMIN_COMPILATIONS_ENDPOINT_PREFIX = "/admin/compilations";
    private static final String PUBLIC_COMPILATIONS_ENDPOINT_PREFIX = "/compilations";

    private final EventService eventService;
    private final CompilationService compilationService;

    // Admin

    @PostMapping(ADMIN_COMPILATIONS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationResponseDto create(
            @Validated(Create.class) @RequestBody CompilationRequestDto compilationRequestDto
    ) {
        final List<Long> eventIds = Optional.ofNullable(compilationRequestDto.getEvents())
                .orElse(Collections.emptyList());
        final List<Event> events = eventIds.size() > 0
                ? eventService.getAllFilterByIds(eventIds)
                : Collections.emptyList();

        final Compilation compilation = map(compilationRequestDto).toBuilder()
                .events(events)
                .build();

        return map(compilationService.create(compilation));
    }

    @PatchMapping(ADMIN_COMPILATIONS_ENDPOINT_PREFIX + "/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationResponseDto update(
            @PathVariable long compId,
            @Validated(Update.class) @RequestBody(required = false) CompilationRequestDto compilationRequestDto
    ) {
        if (compilationRequestDto == null) {
            return map(compilationService.getById(compId));
        }

        final List<Long> eventIds = Optional.of(compilationRequestDto)
                .map(CompilationRequestDto::getEvents)
                .orElse(Collections.emptyList());
        final List<Event> events = eventIds.size() > 0
                ? eventService.getAllFilterByIds(eventIds)
                : Collections.emptyList();

        final Compilation compilation = map(compilationRequestDto).toBuilder()
                .id(compId)
                .events(events)
                .build();

        return map(compilationService.update(compilation));
    }

    @DeleteMapping(ADMIN_COMPILATIONS_ENDPOINT_PREFIX + "/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long compId) {
        compilationService.delete(compId);
    }

    // Public

    @GetMapping(PUBLIC_COMPILATIONS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationResponseDto> getById(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = PAGE_START_FROM_DEFAULT_TEXT, required = false) @Min(0) int from,
            @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_TEXT, required = false) @Min(1) int size
    ) {
        return compilationService.getAll(pinned, from, size).stream()
                .map(CompilationMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping(PUBLIC_COMPILATIONS_ENDPOINT_PREFIX + "/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationResponseDto getById(@PathVariable long compId) {
        return map(compilationService.getById(compId));
    }
}
