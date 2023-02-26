package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventRequestStatusUpdateResponseDto {

    @NotEmpty
    private List<EventRequestDto> confirmedRequests;

    @NotEmpty
    private List<EventRequestDto> rejectedRequests;
}
