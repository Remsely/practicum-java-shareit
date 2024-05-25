package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {
    @EntityGraph(attributePaths = {"owner", "request"})
    List<Item> findByOwnerOrderById(User owner, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "request"})
    @Query(" select i from Item i " +
            "where i.available = true and (lower(i.name) like lower(concat('%', ?1, '%'))" +
            "    or lower(i.description) like lower(concat('%', ?1, '%')))")
    List<Item> search(String query, Pageable pageable);
}
