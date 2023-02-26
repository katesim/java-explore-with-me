package ru.practicum.ewm.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventRequestStatusUpdateRequestDto {

    public enum EventRequestStatusUpdateAction {
        CONFIRMED, REJECTED
    }

    @NotEmpty
    private List<Long> requestIds;

    @NotBlank
    private EventRequestStatusUpdateAction status;
}
