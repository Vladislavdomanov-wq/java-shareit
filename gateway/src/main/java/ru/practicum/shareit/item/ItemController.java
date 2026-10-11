package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient client;

    @PostMapping
    public ResponseEntity<ItemResponseDto> create(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @RequestBody @Valid ItemDto dto) {
        return client.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> update(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto dto) {
        return client.update(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> findById(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long itemId) {
        return client.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> findAll(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId) {
        return client.findAll(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> search(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @RequestParam String text) {
        return client.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> addComment(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid CommentDto commentDto) {
        return client.addComment(userId, itemId, commentDto);
    }
}