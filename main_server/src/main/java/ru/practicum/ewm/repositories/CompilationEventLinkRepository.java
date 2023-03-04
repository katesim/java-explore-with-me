package ru.practicum.ewm.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entities.CompilationEventLink;

import java.util.List;

public interface CompilationEventLinkRepository extends JpaRepository<CompilationEventLink, Long> {

    List<CompilationEventLink> findAllByCompilationIdEqualsAndEventIdIn(long compId, List<Long> eventIds);
}
