package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient client;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @RequestBody @Valid ItemDto dto) {
        return client.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto dto) {
        return client.update(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long itemId) {
        return client.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        return client.findAll();
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @RequestParam String text) {
        return client.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {
        return client.addComment(userId, itemId, commentDto);
    }
}