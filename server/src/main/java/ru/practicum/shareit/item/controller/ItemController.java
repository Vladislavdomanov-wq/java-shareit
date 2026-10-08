package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId, @RequestBody ItemDto dto) {
        return itemService.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto dto) {
        return itemService.update(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId,
                            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId) {
        return itemService.findById(itemId, userId);
    }

    @GetMapping
    public Collection<ItemDto> findByOwner(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId) {
        return itemService.findByOwnerId(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}