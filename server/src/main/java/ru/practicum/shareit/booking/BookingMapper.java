package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        if (booking.getBooker() != null) {
            dto.setBooker(new UserShortDto(
                    booking.getBooker().getId(),
                    booking.getBooker().getName()
            ));
        }

        if (booking.getItem() != null) {
            dto.setItem(new ItemShortDto(
                    booking.getItem().getId(),
                    booking.getItem().getName()
            ));
        }

        return dto;
    }

    public static Booking toBooking(BookingDto dto) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        return booking;
    }
}