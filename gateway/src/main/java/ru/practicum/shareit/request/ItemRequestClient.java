package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";
    private static final ParameterizedTypeReference<ItemRequestResponseDto> REQUEST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<ItemRequestResponseDto>> REQUESTS =
            new ParameterizedTypeReference<>() {
            };

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<ItemRequestResponseDto> create(long userId, ItemRequestDto dto) {
        return post("", userId, dto, REQUEST);
    }

    public ResponseEntity<List<ItemRequestResponseDto>> findAllByUser(long userId) {
        return get("", userId, REQUESTS);
    }

    public ResponseEntity<List<ItemRequestResponseDto>> findAllOther(long userId) {
        return get("/all", userId, REQUESTS);
    }

    public ResponseEntity<ItemRequestResponseDto> findById(long userId, long requestId) {
        return get("/" + requestId, userId, REQUEST);
    }
}