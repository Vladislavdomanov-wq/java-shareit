package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ItemRequestServiceImpl(ItemRequestRepository requestRepository,
                                  UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemRequestDto create(Long requestorId, ItemRequestDto dto) {
        User requestor = userRepository.findById(requestorId);
        if (requestor == null) throw new RuntimeException("Пользователь не найден");
        ItemRequest r = ItemRequestMapper.toItemRequest(dto);
        r.setRequestor(requestor);
        r.setCreated(LocalDateTime.now());
        return ItemRequestMapper.toItemRequestDto(requestRepository.create(r));
    }

    @Override
    public Collection<ItemRequestDto> findByRequestor(Long requestorId) {
        return requestRepository.findByRequestorId(requestorId).stream()
                .map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequestDto> findAll() {
        return requestRepository.findAllOrderByCreatedDesc().stream()
                .map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto findById(Long id) {
        ItemRequest r = requestRepository.findById(id);
        if (r == null) throw new RuntimeException("Запрос не найден");
        return ItemRequestMapper.toItemRequestDto(r);
    }
}