package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        if (!dto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Email уже занят");
        }

        User user = UserMapper.toUser(dto);
        User saved = userRepository.create(user);
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User existing = userRepository.findById(id);
        if (existing == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new ValidationException("Email уже занят");
            }
        }

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());

        return UserMapper.toUserDto(userRepository.update(existing));
    }

    @Override
    public UserDto findById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        User existing = userRepository.findById(id);
        if (existing == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.delete(id);
    }
}