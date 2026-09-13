package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.Collection;

public interface BookingRepository {
    Booking create(Booking booking);

    Booking update(Booking booking);

    Booking findById(Long id);

    Collection<Booking> findByBookerId(Long bookerId);

    Collection<Booking> findByItemId(Long itemId);

    Collection<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status);

    Collection<Booking> findByItemIdAndBookerIdAndEndBefore(Long itemId, Long bookerId, java.time.LocalDateTime end);

    boolean existsByItemIdAndStartBeforeAndEndAfter(Long itemId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}