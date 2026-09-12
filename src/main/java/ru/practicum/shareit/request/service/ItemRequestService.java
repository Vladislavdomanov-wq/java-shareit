package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto create(Long requestorId, ItemRequestDto dto);
    Collection<ItemRequestDto> findByRequestor(Long requestorId);
    Collection<ItemRequestDto> findAll();
    ItemRequestDto findById(Long id);
}