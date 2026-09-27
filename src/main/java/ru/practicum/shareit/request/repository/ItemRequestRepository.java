package ru.practicum.shareit.request.repository;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;

public interface ItemRequestRepository {
    ItemRequest create(ItemRequest request);

    Collection<ItemRequest> findByRequestorId(Long requestorId);

    Collection<ItemRequest> findAllOrderByCreatedDesc();

    ItemRequest findById(Long id);
}
