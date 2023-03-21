package ru.practicum.ewm.controllers.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class CommentRequestDto {

    @NotBlank
    private String text;
}
