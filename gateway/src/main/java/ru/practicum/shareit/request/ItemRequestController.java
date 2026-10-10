package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient client;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @RequestBody @Valid ItemRequestDto dto) {
        return client.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUser(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId) {
        return client.findAllByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllOther(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId) {
        return client.findAllOther(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long requestId) {
        return client.findById(userId, requestId);
    }
}