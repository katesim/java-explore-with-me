package ru.practicum.ewm.hit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository repository;

    @Override
    @Transactional
    public Hit add(Hit hit) {
        return repository.save(hit);
    }
}
