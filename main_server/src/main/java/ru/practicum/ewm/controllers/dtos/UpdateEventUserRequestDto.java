package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UpdateEventUserRequestDto extends EventBaseDto {

    private Long category;

    private UpdateEventStateUserAction stateAction;
}
