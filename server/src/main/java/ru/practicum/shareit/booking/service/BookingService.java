package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.Collection;

public interface BookingService {
    BookingDto create(Long bookerId, BookingDto dto);

    BookingDto approve(Long ownerId, Long bookingId, Boolean approved);

    BookingDto findById(Long userId, Long bookingId);

    Collection<BookingDto> findByBooker(Long bookerId, BookingState state);

    Collection<BookingDto> findByOwner(Long ownerId, BookingState state);

}