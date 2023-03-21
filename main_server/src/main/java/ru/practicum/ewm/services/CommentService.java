package ru.practicum.ewm.services;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.entities.Comment;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;

public interface CommentService {

    Comment create(final Comment comment);

    Page<Comment> getAll(int from, int size);

    Page<Comment> getAllByEventId(long eventId, int from, int size);

    Comment getById(long commentId) throws NotFoundException;

    Comment update(final Comment comment) throws NotFoundException, ForbiddenOperation;

    void deleteById(long commentId, long userId, long eventId) throws NotFoundException, ForbiddenOperation;

    void deleteById(long commentId) throws NotFoundException;
}
