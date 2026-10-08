package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;


public interface ItemService {
    ItemDto create(Long ownerId, ItemDto dto);

    ItemDto update(Long ownerId, Long itemId, ItemDto dto);

    ItemDto findById(Long itemId, Long userId);

    Collection<ItemDto> findByOwnerId(Long ownerId);

    Collection<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
