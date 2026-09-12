package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking b) {
        return new BookingDto(
                b.getId(),
                b.getStart(),
                b.getEnd(),
                b.getItem() != null ? b.getItem().getId() : null,
                b.getBooker() != null ? b.getBooker().getId() : null,
                b.getStatus()
        );
    }

    public static Booking toBooking(BookingDto dto) {
        return new Booking(
                dto.getId(),
                dto.getStart(),
                dto.getEnd(),
                null,
                null,
                dto.getStatus()
        );
    }
}