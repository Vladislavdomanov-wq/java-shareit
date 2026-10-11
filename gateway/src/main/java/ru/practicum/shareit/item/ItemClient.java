package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";
    private static final ParameterizedTypeReference<ItemResponseDto> ITEM =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<ItemResponseDto>> ITEMS =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<CommentResponseDto> COMMENT =
            new ParameterizedTypeReference<>() {
            };

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<ItemResponseDto> create(long userId, ItemDto dto) {
        return post("", userId, dto, ITEM);
    }

    public ResponseEntity<ItemResponseDto> update(long userId, long itemId, ItemDto dto) {
        return patch("/" + itemId, userId, dto, ITEM);
    }

    public ResponseEntity<ItemResponseDto> findById(long userId, long itemId) {
        return get("/" + itemId, userId, ITEM);
    }

    public ResponseEntity<List<ItemResponseDto>> findAll(long userId) {
        return get("", userId, ITEMS);
    }

    public ResponseEntity<List<ItemResponseDto>> search(long userId, String text) {
        Map<String, Object> parameters = Map.of("text", text);
        return get("/search?text={text}", userId, parameters, ITEMS);
    }

    public ResponseEntity<CommentResponseDto> addComment(long userId, Long itemId, CommentDto commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto, COMMENT);
    }
}