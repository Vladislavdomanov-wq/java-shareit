package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item create(Item item);

    Item update(Item item);

    Item findById(Long id);

    Collection<Item> findAll();

    Collection<Item> findByOwnerId(Long ownerId);

    Collection<Item> searchByText(String text);
}
