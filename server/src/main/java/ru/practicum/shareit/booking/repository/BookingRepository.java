package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    List<Booking> findAllByItem_OwnerId(Long ownerId, Sort sort);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND " +
            "(b.start < :end AND b.end > :start)")
    boolean existsByItemIdAndTimeOverlap(@Param("itemId") Long itemId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId")
    List<Booking> findAllByItem_Id(@Param("itemId") Long itemId);
}