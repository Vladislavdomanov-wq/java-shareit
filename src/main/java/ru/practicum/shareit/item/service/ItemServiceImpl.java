package ru.practicum.shareit.item.service;


import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        User owner = userRepository.findById(ownerId);
        if (owner == null) {
            throw new NotFoundException("Владелец не найден");
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }

        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);

        Item saved = itemRepository.create(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        Item existing = itemRepository.findById(itemId);
        if (existing == null) {
            throw new RuntimeException("Вещь не найдена");
        }

        if (!existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может редактировать вещь");
        }

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());

        Item updated = itemRepository.update(existing);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            throw new RuntimeException("Вещь не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> findByOwnerId(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(String text) {
        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}

