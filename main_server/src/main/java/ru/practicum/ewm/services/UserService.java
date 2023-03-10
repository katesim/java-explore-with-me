package ru.practicum.ewm.services;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;

public interface UserService {

    User create(User user);

    User get(long userId) throws NotFoundException;

    Page<User> getAll(int from, int size);

    Page<User> getAllWithUserIds(List<Long> userIds, int from, int size);

    void delete(long userId) throws NotFoundException;
}
