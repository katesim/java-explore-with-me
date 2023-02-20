package ru.practicum.ewm.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entities.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByUserIdInOrderByUserIdDesc(List<Long> userIds, Pageable pageable);
}
