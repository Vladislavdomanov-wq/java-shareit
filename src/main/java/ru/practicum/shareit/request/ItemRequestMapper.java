package ru.practicum.shareit.request;


import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor() != null ? itemRequest.getRequestor().getId() : null,
                itemRequest.getCreated()
        );

    }

    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return new ItemRequest(
                dto.getId(),
                dto.getDescription(),
                null,
                dto.getCreated()
        );
    }
}
