package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";
    private static final ParameterizedTypeReference<UserResponseDto> USER =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<UserResponseDto>> USERS =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Void> NOTHING =
            new ParameterizedTypeReference<>() {
            };

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<UserResponseDto> create(UserDto dto) {
        return post("", null, dto, USER);
    }

    public ResponseEntity<UserResponseDto> update(long userId, UserDto dto) {
        return patch("/" + userId, null, dto, USER);
    }

    public ResponseEntity<UserResponseDto> findById(long userId) {
        return get("/" + userId, null, USER);
    }

    public ResponseEntity<List<UserResponseDto>> findAll() {
        return get("", null, USERS);
    }

    public ResponseEntity<Void> delete(Long userId) {
        return delete("/" + userId, null, NOTHING);
    }
}