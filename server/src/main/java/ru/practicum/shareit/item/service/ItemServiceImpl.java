package ru.practicum.shareit.item.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           CommentRepository commentRepository,
                           BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Владелец не найден"));

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }

        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может редактировать вещь");
        }

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());

        Item updated = itemRepository.save(existing);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto dto = ItemMapper.toItemDto(item);

        List<CommentDto> comments = commentRepository.findByItem_Id(itemId).stream()
                .map(comment -> {
                    CommentDto cDto = new CommentDto();
                    cDto.setId(comment.getId());
                    cDto.setText(comment.getText());
                    cDto.setAuthorName(comment.getAuthor().getName());
                    cDto.setCreated(comment.getCreated());
                    return cDto;
                })
                .sorted(Comparator.comparing(CommentDto::getCreated).reversed())
                .collect(Collectors.toList());
        dto.setComments(comments);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings = bookingRepository.findAllByItem_Id(itemId);

            bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.APPROVED && !b.getStart().isAfter(now))
                    .max(Comparator.comparing(Booking::getStart))
                    .ifPresent(b -> dto.setLastBooking(new BookingShortDto(b.getId(), b.getBooker().getId())));

            bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .ifPresent(b -> dto.setNextBooking(new BookingShortDto(b.getId(), b.getBooker().getId())));
        }

        return dto;
    }

    @Override
    public Collection<ItemDto> findByOwnerId(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        System.out.println("=== addComment вызван ===");
        System.out.println("userId: " + userId + ", itemId: " + itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookings = bookingRepository.findAllByItem_Id(item.getId());
        System.out.println("Найдено бронирований для itemId=" + itemId + ": " + bookings.size());
        bookings.forEach(b -> {
            System.out.println("  Booking id=" + b.getId() +
                    ", status=" + b.getStatus() +
                    ", end=" + b.getEnd() +
                    ", end < now: " + b.getEnd().isBefore(LocalDateTime.now()));
        });

        boolean hasApprovedBooking = bookings.stream()
                .anyMatch(b -> b.getBooker().getId().equals(userId)
                        && b.getStatus() == BookingStatus.APPROVED
                        && b.getEnd().isBefore(LocalDateTime.now()));

        System.out.println("hasApprovedBooking: " + hasApprovedBooking);

        if (!hasApprovedBooking) {
            throw new IllegalArgumentException("Нельзя оставить отзыв: вы не брали эту вещь в аренду или срок аренды не истёк");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        System.out.println("Комментарий сохранён: id=" + saved.getId());

        CommentDto result = new CommentDto();
        result.setId(saved.getId());
        result.setText(saved.getText());
        result.setAuthorName(saved.getAuthor().getName());
        result.setCreated(saved.getCreated());

        return result;
    }
}

