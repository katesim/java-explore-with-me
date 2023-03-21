package ru.practicum.ewm.controllers.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.controllers.dtos.CommentRequestDto;
import ru.practicum.ewm.controllers.dtos.CommentDto;
import ru.practicum.ewm.controllers.dtos.UserShortDto;
import ru.practicum.ewm.entities.Comment;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.utils.DateTimeUtils;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static Comment map(CommentRequestDto commentRequestDto) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .build();
    }

    public static CommentDto map(Comment comment) {
        final User initiator = comment.getUser();
        final UserShortDto initiatorDto = UserShortDto.builder()
                .id(initiator.getId())
                .name(initiator.getName())
                .build();

        return CommentDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent().getId())
                .user(initiatorDto)
                .createdOn(DateTimeUtils.format(comment.getCreatedOn()))
                .editedOn(DateTimeUtils.format(comment.getEditedOn()))
                .text(comment.getText())
                .build();
    }
}
