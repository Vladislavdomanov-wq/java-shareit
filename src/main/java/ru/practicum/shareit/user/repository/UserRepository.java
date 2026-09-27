package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    User create(User user);

    User update(User user);

    User findById(Long id);

    Collection<User> findAll();

    boolean existsByEmail(String email);

    void delete(Long id);
}