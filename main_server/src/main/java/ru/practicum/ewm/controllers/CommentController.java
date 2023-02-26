package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.controllers.dtos.CommentDto;
import ru.practicum.ewm.controllers.dtos.CommentRequestDto;
import ru.practicum.ewm.controllers.mappers.CommentMapper;
import ru.practicum.ewm.entities.Comment;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.services.CommentService;
import ru.practicum.ewm.services.EventService;
import ru.practicum.ewm.services.UserService;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.common.EWMConstants.PAGE_SIZE_DEFAULT_TEXT;
import static ru.practicum.ewm.common.EWMConstants.PAGE_START_FROM_DEFAULT_TEXT;
import static ru.practicum.ewm.controllers.mappers.CommentMapper.map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final EventService eventService;

    private static final String ADMIN_COMMENTS_ENDPOINT_PREFIX = "/admin/comments";
    private static final String USER_COMMENTS_ENDPOINT_PREFIX = "/users/{userId}/events/{eventId}/comments";
    private static final String PUBLIC_COMMENTS_ENDPOINT_PREFIX = "/events/{eventId}/comments";

    // Admin

    @GetMapping(ADMIN_COMMENTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAll(
            @RequestParam(defaultValue = PAGE_START_FROM_DEFAULT_TEXT, required = false) @Min(0) int from,
            @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_TEXT, required = false) @Min(1) int size
    ) {
        return commentService.getAll(from, size).stream()
                .map(CommentMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping(ADMIN_COMMENTS_ENDPOINT_PREFIX + "/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getById(
            @PathVariable long commentId
    ) {
        return map(commentService.getById(commentId));
    }

    @DeleteMapping(ADMIN_COMMENTS_ENDPOINT_PREFIX + "/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(
            @PathVariable long commentId
    ) {
        commentService.deleteById(commentId);
    }

    // User

    @PostMapping(USER_COMMENTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(
            @PathVariable long userId,
            @PathVariable long eventId,
            @Validated @RequestBody CommentRequestDto commentDto
    ) {
        final User user = userService.get(userId);
        final Event event = eventService.getById(eventId);

        final Comment comment = map(commentDto).toBuilder()
                .event(event)
                .user(user)
                .build();

        final Comment savedComment = commentService.create(comment);
        eventService.addComment(savedComment, eventId);

        log.info("Create comment: {}", comment);
        return map(savedComment);
    }

    @PatchMapping(USER_COMMENTS_ENDPOINT_PREFIX + "/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto update(
            @PathVariable long userId,
            @PathVariable long eventId,
            @PathVariable long commentId,
            @Validated @RequestBody CommentRequestDto commentDto
    ) {
        final User user = userService.get(userId);
        final Event event = eventService.getById(eventId);
        Comment comment = map(commentDto).toBuilder()
                .id(commentId)
                .user(user)
                .event(event)
                .build();

        return map(commentService.update(comment));
    }

    @DeleteMapping(USER_COMMENTS_ENDPOINT_PREFIX + "/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable long userId,
            @PathVariable long eventId,
            @PathVariable long commentId
    ) {
        commentService.deleteById(commentId, userId, eventId);
    }

    // Public

    @GetMapping(PUBLIC_COMMENTS_ENDPOINT_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAll(
            @PathVariable long eventId,
            @RequestParam(defaultValue = PAGE_START_FROM_DEFAULT_TEXT, required = false) @Min(0) int from,
            @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_TEXT, required = false) @Min(1) int size
    ) {
        return commentService.getAllByEventId(eventId, from, size).stream()
                .map(CommentMapper::map)
                .collect(Collectors.toList());
    }
}
