package ru.practicum.shareit.booking.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<Long, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Booking create(Booking b) {
        b.setId(nextId.getAndIncrement());
        bookings.put(b.getId(), b);
        return b;
    }

    @Override
    public Booking update(Booking b) {
        bookings.put(b.getId(), b);
        return b;
    }

    @Override
    public Booking findById(Long id) {
        return bookings.get(id);
    }

    @Override
    public Collection<Booking> findByBookerId(Long bookerId) {
        return bookings.values().stream()
                .filter(b -> b.getBooker().getId().equals(bookerId))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Booking> findByItemId(Long itemId) {
        return bookings.values().stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status) {
        return findByBookerId(bookerId).stream()
                .filter(b -> b.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Booking> findByItemIdAndBookerIdAndEndBefore(
            Long itemId, Long bookerId, LocalDateTime end) {
        return bookings.values().stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .filter(b -> b.getBooker().getId().equals(bookerId))
                .filter(b -> b.getEnd().isBefore(end))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByItemIdAndStartBeforeAndEndAfter(
            Long itemId, LocalDateTime start, LocalDateTime end) {
        return bookings.values().stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .anyMatch(b -> b.getStart().isBefore(end) && b.getEnd().isAfter(start));
    }
}