package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto dto);

    UserDto update(Long id, UserDto dto);

    UserDto findById(Long id);

    Collection<UserDto> findAll();

    void delete(Long id);
}