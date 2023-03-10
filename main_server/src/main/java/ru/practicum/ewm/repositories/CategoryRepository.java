package ru.practicum.ewm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
