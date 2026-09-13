package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;

public interface BookingService {
    BookingDto create(Long bookerId, BookingDto dto);

    BookingDto approve(Long ownerId, Long bookingId, Boolean approved);

    BookingDto findById(Long bookingId);

    Collection<BookingDto> findByBooker(Long bookerId, String state);

    Collection<BookingDto> findByOwner(Long ownerId, String state);
}