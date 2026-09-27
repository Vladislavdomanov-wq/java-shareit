package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BookingDto create(Long bookerId, BookingDto dto) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) throw new RuntimeException("Вещь недоступна");
        if (item.getOwner().getId().equals(bookerId))
            throw new RuntimeException("Нельзя бронировать свою вещь");

        if (dto.getStart() == null || dto.getEnd() == null)
            throw new RuntimeException("Даты должны быть указаны");
        if (!dto.getStart().isBefore(dto.getEnd()))
            throw new RuntimeException("Дата начала должна быть раньше даты конца");
        if (dto.getStart().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Нельзя бронировать в прошлом");

        if (bookingRepository.existsByItemIdAndTimeOverlap(item.getId(), dto.getStart(), dto.getEnd())) {
            throw new RuntimeException("Вещь уже забронирована на эти даты");
        }

        Booking booking = BookingMapper.toBooking(dto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId))
            throw new RuntimeException("Только владелец может подтвердить бронирование");

        if (booking.getStatus() != BookingStatus.WAITING)
            throw new RuntimeException("Можно подтвердить только ожидание");

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);

        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ForbiddenException("Нет прав на просмотр этого бронирования");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> findByBooker(Long bookerId, String state) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return filterByState(
                bookingRepository.findAllByBookerId(bookerId, Sort.by(Sort.Direction.DESC, "start")),
                state
        ).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> findByOwner(Long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> allBookings = bookingRepository.findAllByItem_OwnerId(ownerId, Sort.by(Sort.Direction.DESC, "start"));
        return filterByState(allBookings, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }


    private List<Booking> filterByState(List<Booking> bookings, String state) {
        if (state == null || "ALL".equals(state)) {
            return bookings;
        }

        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case "CURRENT":
                return bookings.stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
            case "PAST":
                return bookings.stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookings.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
            case "WAITING":
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.WAITING)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}