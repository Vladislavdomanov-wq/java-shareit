package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient client;

    @PostMapping
    public ResponseEntity<ItemRequestResponseDto> create(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @RequestBody @Valid ItemRequestDto dto) {
        return client.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestResponseDto>> findAllByUser(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId) {
        return client.findAllByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestResponseDto>> findAllOther(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId) {
        return client.findAllOther(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestResponseDto> findById(
            @RequestHeader(HeaderConstants.X_SHARER_USER_ID) long userId,
            @PathVariable Long requestId) {
        return client.findById(userId, requestId);
    }
}