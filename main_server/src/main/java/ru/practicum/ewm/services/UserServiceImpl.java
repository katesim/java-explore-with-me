package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String NOT_FOUND_MSG_FORMAT = "User with id=%d was not found";

    private final UserRepository repo;

    @Override
    @Transactional
    public User create(User user) {
        return repo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User get(long userId) throws NotFoundException {
        Optional<User> user = repo.findById(userId);
        if (user.isEmpty()) {
            final String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return user.get();
    }

    @Override
    public Page<User> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.findAll(pageable);
    }

    @Override
    public Page<User> getAllWithUserIds(List<Long> userIds, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repo.findByUserIdInOrderByUserIdDesc(userIds, pageable);
    }

    @Override
    @Transactional
    public void delete(long userId) throws NotFoundException {
        this.get(userId);
        repo.deleteById(userId);
    }
}
