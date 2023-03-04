package ru.practicum.ewm.controllers.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.controllers.dtos.CompilationRequestDto;
import ru.practicum.ewm.controllers.dtos.CompilationResponseDto;
import ru.practicum.ewm.entities.Compilation;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation map(CompilationRequestDto compilationRequestDto) {
        return Compilation.builder()
                .title(compilationRequestDto.getTitle())
                .pinned(Optional.ofNullable(compilationRequestDto.getPinned())
                        .orElse(false))
                .build();
    }

    public static CompilationResponseDto map(Compilation compilation) {
        return CompilationResponseDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(Optional.ofNullable(compilation.getEvents())
                        .map(list -> list.stream()
                                .map(EventMapper::map)
                                .collect(Collectors.toList())
                        )
                        .orElse(Collections.emptyList()))
                .build();
    }
}
