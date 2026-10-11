package ru.practicum.shareit.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.config.HeaderConstants;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <R> ResponseEntity<R> get(String path, @Nullable Long userId,
                                        ParameterizedTypeReference<R> responseType) {
        return send(HttpMethod.GET, path, userId, null, null, responseType);
    }

    protected <R> ResponseEntity<R> get(String path, @Nullable Long userId,
                                        @Nullable Map<String, Object> parameters,
                                        ParameterizedTypeReference<R> responseType) {
        return send(HttpMethod.GET, path, userId, parameters, null, responseType);
    }

    protected <T, R> ResponseEntity<R> post(String path, @Nullable Long userId, T body,
                                            ParameterizedTypeReference<R> responseType) {
        return send(HttpMethod.POST, path, userId, null, body, responseType);
    }

    protected <T, R> ResponseEntity<R> patch(String path, @Nullable Long userId, T body,
                                             ParameterizedTypeReference<R> responseType) {
        return send(HttpMethod.PATCH, path, userId, null, body, responseType);
    }

    protected <T, R> ResponseEntity<R> patch(String path, @Nullable Long userId,
                                             @Nullable Map<String, Object> parameters, @Nullable T body,
                                             ParameterizedTypeReference<R> responseType) {
        return send(HttpMethod.PATCH, path, userId, parameters, body, responseType);
    }

    protected <R> ResponseEntity<R> delete(String path, @Nullable Long userId,
                                           ParameterizedTypeReference<R> responseType) {
        return send(HttpMethod.DELETE, path, userId, null, null, responseType);
    }

    private <T, R> ResponseEntity<R> send(HttpMethod method, String path, @Nullable Long userId,
                                          @Nullable Map<String, Object> parameters, @Nullable T body,
                                          ParameterizedTypeReference<R> responseType) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        if (parameters != null) {
            return rest.exchange(path, method, requestEntity, responseType, parameters);
        }
        return rest.exchange(path, method, requestEntity, responseType);
    }

    private HttpHeaders defaultHeaders(@Nullable Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set(HeaderConstants.X_SHARER_USER_ID, String.valueOf(userId));
        }
        return headers;
    }
}