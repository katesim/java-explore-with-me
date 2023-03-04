package ru.practicum.ewm.services;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.entities.Compilation;
import ru.practicum.ewm.exceptions.NotFoundException;

public interface CompilationService {

    Compilation create(Compilation compilation);

    Compilation update(Compilation compilation) throws NotFoundException;

    Compilation getById(long compId) throws NotFoundException;

    Page<Compilation> getAll(Boolean pinned, int from, int size);

    void delete(long compId) throws NotFoundException;
}
