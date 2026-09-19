package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;


import java.util.Collection;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
                                 @RequestBody ItemRequestDto dto) {
        return service.create(userId, dto);
    }

    @GetMapping
    public Collection<ItemRequestDto> findByRequestor(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId) {
        return service.findByRequestor(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ItemRequestDto findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
