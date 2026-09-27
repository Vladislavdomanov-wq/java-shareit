package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
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
        User booker = userRepository.findById(bookerId);
        if (booker == null) throw new RuntimeException("Пользователь не найден");

        Item item = itemRepository.findById(dto.getItemId());
        if (item == null) throw new RuntimeException("Вещь не найдена");
        if (!item.getAvailable()) throw new RuntimeException("Вещь недоступна");
        if (item.getOwner().getId().equals(bookerId))
            throw new RuntimeException("Нельзя бронировать свою вещь");

        if (dto.getStart() == null || dto.getEnd() == null)
            throw new RuntimeException("Даты должны быть указаны");
        if (!dto.getStart().isBefore(dto.getEnd()))
            throw new RuntimeException("Дата начала должна быть раньше даты конца");
        if (dto.getStart().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Нельзя бронировать в прошлом");

        if (bookingRepository.existsByItemIdAndStartBeforeAndEndAfter(
                item.getId(), dto.getStart(), dto.getEnd())) {
            throw new RuntimeException("Вещь уже забронирована на эти даты");
        }

        Booking booking = BookingMapper.toBooking(dto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.create(booking));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) throw new RuntimeException("Бронирование не найдено");

        if (!booking.getItem().getOwner().getId().equals(ownerId))
            throw new RuntimeException("Только владелец может подтвердить бронирование");

        if (booking.getStatus() != BookingStatus.WAITING)
            throw new RuntimeException("Можно подтвердить только ожидание");

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.update(booking));
    }

    @Override
    public BookingDto findById(Long bookingId) {
        Booking b = bookingRepository.findById(bookingId);
        if (b == null) throw new RuntimeException("Бронирование не найдено");
        return BookingMapper.toBookingDto(b);
    }

    @Override
    public Collection<BookingDto> findByBooker(Long bookerId, String state) {
        return filterByState(bookingRepository.findByBookerId(bookerId), state).stream()
                .map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> findByOwner(Long ownerId, String state) {
        Collection<Booking> allBookings = itemRepository.findByOwnerId(ownerId).stream()
                .flatMap(item -> bookingRepository.findByItemId(item.getId()).stream())
                .distinct()
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        return filterByState(allBookings, state).stream()
                .map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private Collection<Booking> filterByState(Collection<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case "CURRENT" -> bookings.stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .collect(Collectors.toList());
            case "PAST" -> bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .collect(Collectors.toList());
            case "FUTURE" -> bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .collect(Collectors.toList());
            case "WAITING" -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.WAITING)
                    .collect(Collectors.toList());
            case "REJECTED" -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                    .collect(Collectors.toList());
            case "ALL" -> bookings;
            default -> throw new RuntimeException("Неизвестный статус: " + state);
        };
    }
}