package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository requestRepository,
                                  UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto create(Long requestorId, ItemRequestDto dto) {
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest r = ItemRequestMapper.toItemRequest(dto);
        r.setRequestor(requestor);
        r.setCreated(LocalDateTime.now());

        ItemRequest saved = requestRepository.save(r);
        return ItemRequestMapper.toItemRequestDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestDto> findByRequestor(Long requestorId) {
        checkUser(requestorId);
        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId).stream()
                .map(this::toDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestDto> findAll(Long userId) {
        checkUser(userId);
        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(this::toDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto findById(Long userId, Long id) {
        checkUser(userId);
        ItemRequest r = requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        return toDtoWithItems(r);
    }

    private void checkUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private ItemRequestDto toDtoWithItems(ItemRequest r) {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(r);
        List<Item> items = itemRepository.findAllByRequestId(r.getId());
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);
        return dto;
    }
}