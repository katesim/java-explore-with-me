package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entities.Compilation;
import ru.practicum.ewm.entities.CompilationEventLink;
import ru.practicum.ewm.entities.Event;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.CompilationEventLinkRepository;
import ru.practicum.ewm.repositories.CompilationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private static final String NOT_FOUND_MSG_FORMAT = "Compilation with id=%d was not found";

    private final CompilationRepository compilationRepository;
    private final CompilationEventLinkRepository compilationEventLinkRepository;

    @Override
    @Transactional
    public Compilation create(Compilation compilation) {
        return compilationRepository.save(compilation);
    }

    @Override
    @Transactional
    public Compilation update(Compilation updateCompilation) throws NotFoundException {
        final Compilation compilation = this.getById(updateCompilation.getId());

        // update compilation
        final Compilation.CompilationBuilder updatedCompilationBuilder = compilation.toBuilder();

        if (updateCompilation.getTitle() != null
                && !compilation.getTitle().equals(updateCompilation.getTitle())) {
            updatedCompilationBuilder.title(updateCompilation.getTitle());
        }

        if (updateCompilation.getPinned() != null
                && !compilation.getPinned().equals(updateCompilation.getPinned())) {
            updatedCompilationBuilder.pinned(updateCompilation.getPinned());
        }

        if (updateCompilation.getEvents() != null
                && !compilation.getEvents().equals(updateCompilation.getEvents())) {

            final List<Long> oldEventIds = compilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());
            final List<Long> newEventIds = updateCompilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());

            final List<Long> addedEventIds = newEventIds.stream()
                    .filter(id -> !oldEventIds.contains(id))
                    .collect(Collectors.toList());
            final List<Long> removedEventIds = oldEventIds.stream()
                    .filter(id -> !newEventIds.contains(id))
                    .collect(Collectors.toList());

            // add new links
            final List<CompilationEventLink> addedEventLinks = addedEventIds.stream()
                    .map(eventId -> CompilationEventLink.builder()
                            .compilationId(compilation.getId())
                            .eventId(eventId)
                            .build())
                    .collect(Collectors.toList());
            compilationEventLinkRepository.saveAll(addedEventLinks);

            // remove old links
            final List<CompilationEventLink> removedEventLinks = compilationEventLinkRepository
                    .findAllByCompilationIdEqualsAndEventIdIn(
                            compilation.getId(), removedEventIds
                    );
            compilationEventLinkRepository.deleteAll(removedEventLinks);

            // skip already inserted links
            updatedCompilationBuilder.events(null);
        }

        return compilationRepository.save(updatedCompilationBuilder.build());
    }

    @Override
    @Transactional(readOnly = true)
    public Compilation getById(long compId) throws NotFoundException {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty()) {
            final String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, compId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return compilation.get();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Compilation> getAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return pinned != null
                ? compilationRepository.findAllByPinnedEquals(pinned, pageable)
                : compilationRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void delete(long compId) throws NotFoundException {
        this.getById(compId);
        compilationRepository.deleteById(compId);
    }
}
