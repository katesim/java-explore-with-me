package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entities.Comment;
import ru.practicum.ewm.exceptions.ForbiddenOperation;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.CommentRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final String NOT_FOUND_MSG_FORMAT = "Comment with id=%d was not found";
    private static final String NOT_ALLOWED_FOR_USER_ERROR_MSG_FORMAT = "Comment with id=%d is not owned by userId=%d";
    private static final String NOT_ALLOWED_FOR_EVENT_ERROR_MSG_FORMAT = "Comment with id=%d is not linked to eventId=%d";

    private final CommentRepository repo;

    @Override
    @Transactional
    public Comment create(final Comment comment) {
        final LocalDateTime now = LocalDateTime.now();
        final Comment newComment = comment.toBuilder()
                .createdOn(now)
                .editedOn(now)
                .build();

        return repo.save(newComment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getAllByEventId(long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.findAllByEventId(eventId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Comment getById(long commentId) throws NotFoundException {
        Optional<Comment> comment = repo.findById(commentId);
        if (comment.isEmpty()) {
            final String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, commentId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return comment.get();
    }

    @Override
    @Transactional
    public Comment update(
            final Comment updateComment
    ) throws NotFoundException, ForbiddenOperation {
        final Comment comment = getById(updateComment.getId());

        if (!Objects.equals(comment.getUser().getId(), updateComment.getUser().getId())) {
            final String errorMessage = String.format(NOT_ALLOWED_FOR_USER_ERROR_MSG_FORMAT, updateComment.getId(), updateComment.getUser().getId());
            log.error(errorMessage);
            throw new ForbiddenOperation(errorMessage);
        }

        final Comment.CommentBuilder updatedCommentBuilder = comment.toBuilder();

        final LocalDateTime now = LocalDateTime.now();
        updatedCommentBuilder.editedOn(now);
        updatedCommentBuilder.text(updateComment.getText());

        final Comment updatedComment = updatedCommentBuilder.build();
        return repo.save(updatedComment);
    }

    @Override
    @Transactional
    public void deleteById(
            long commentId,
            long userId,
            long eventId
    ) throws NotFoundException, ForbiddenOperation {
        final Comment comment = this.getById(commentId);

        if (comment.getUser().getId() != userId) {
            final String errorMessage = String.format(NOT_ALLOWED_FOR_USER_ERROR_MSG_FORMAT, commentId, userId);
            log.error(errorMessage);
            throw new ForbiddenOperation(errorMessage);
        }

        if (comment.getEvent().getId() != eventId) {
            final String errorMessage = String.format(NOT_ALLOWED_FOR_EVENT_ERROR_MSG_FORMAT, commentId, eventId);
            log.error(errorMessage);
            throw new ForbiddenOperation(errorMessage);
        }

        repo.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteById(long commentId) throws NotFoundException {
        getById(commentId);
        repo.deleteById(commentId);
    }
}
