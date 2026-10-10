package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.shareit.user.dto.UserDto;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid UserDto dto) {
        return client.create(dto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(
            @PathVariable Long userId,
            @RequestBody UserDto dto) {
        return client.update(userId, dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> findById(@PathVariable Long userId) {
        return client.findById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        return client.findAll();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        return client.delete(userId);
    }
}
