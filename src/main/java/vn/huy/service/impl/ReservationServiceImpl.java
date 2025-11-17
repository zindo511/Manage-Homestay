package vn.huy.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.huy.common.PaymentStatus;
import vn.huy.common.ReservationStatus;
import vn.huy.controller.request.ReservationCreationRequest;
import vn.huy.controller.response.ReservationResponse;
import vn.huy.exception.ResourceNotFoundException;
import vn.huy.model.Reservation;
import vn.huy.model.Room;
import vn.huy.model.User;
import vn.huy.repository.ReservationRepository;
import vn.huy.repository.RoomRepository;
import vn.huy.repository.UserRepository;
import vn.huy.service.ReservationService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    @Override
    public ReservationResponse createReservation(ReservationCreationRequest request) {
        User user = getUser(request.getUserId());
        Room room = getRoom(request.getRoomId());
        User employee = null;

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (request.getNumGuests() > room.getCapacity()) {
            throw new IllegalArgumentException("Number of guests exceeds room capacity");
        }

        int nights = (int) Duration.between(request.getCheckInDate(), request.getCheckOutDate()).toDays();
        BigDecimal roomPrice = room.getPrice();
        BigDecimal total = roomPrice.multiply(BigDecimal.valueOf(nights));

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setEmployee(employee);
        reservation.setBookingDate(LocalDateTime.now());
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setNights(nights);
        reservation.setNumGuests(request.getNumGuests());
        reservation.setRoomPrice(roomPrice);
        reservation.setTotal(total);
        reservation.setStatus(ReservationStatus.Pending);
        reservation.setPaymentStatus(PaymentStatus.Unpaid);
        reservation.setPaymentMethod(request.getPaymentMethod());

        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Override
    public ReservationResponse updateReservation(Long id, ReservationCreationRequest req) {
        Reservation reservation = getReservation(id);

        if (reservation.getStatus() == ReservationStatus.Cancelled ||
                reservation.getStatus() == ReservationStatus.Checked_out) {
            throw new IllegalStateException("Cannot update cancelled or checked-out reservation");
        }

        // Allow update day
        if (req.getCheckInDate() != null && req.getCheckOutDate() != null) {
            if (!req.getCheckOutDate().isAfter(req.getCheckInDate())) {
                throw new IllegalArgumentException("Check-out date must be after check-in date");
            }
            reservation.setCheckInDate(req.getCheckInDate());
            reservation.setCheckOutDate(req.getCheckOutDate());
            int nights = (int) Duration.between(req.getCheckInDate(), req.getCheckOutDate()).toDays();
            reservation.setNights(nights);
            reservation.setTotal(reservation.getRoomPrice().multiply(BigDecimal.valueOf(nights)));
        }

        // Allow edit number_guest
        if (req.getNumGuests() != null) {
            if (req.getNumGuests() > reservation.getRoom().getCapacity()) {
                throw new IllegalArgumentException("Number of guests exceeds room capacity");
            }
            reservation.setNumGuests(req.getNumGuests());
        }

        // Allow edit payment_method
        if (req.getPaymentMethod() != null) {
            reservation.setPaymentMethod(req.getPaymentMethod());
        }
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Override
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = getReservation(id);
        if (reservation.getStatus() == ReservationStatus.Checked_out) {
            throw new IllegalStateException("Cannot cancel reservation");
        }

        reservation.setStatus(ReservationStatus.Cancelled);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Override
    public List<ReservationResponse> getReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(this::toResponse).toList();
    }

    /* ==========
        HELPER
    ========== */
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse res = new ReservationResponse();
        res.setId(reservation.getId());
        res.setUserId(reservation.getUser().getId());
        res.setRoomId(reservation.getRoom().getId());
        res.setEmployeeId(reservation.getEmployee().getId());
        res.setBookingDate(reservation.getBookingDate());
        res.setCheckInDate(reservation.getCheckInDate());
        res.setCheckOutDate(reservation.getCheckOutDate());
        res.setNights(reservation.getNights());
        res.setNumGuests(reservation.getNumGuests());
        res.setRoomPrice(reservation.getRoomPrice());
        res.setStatus(reservation.getStatus());
        res.setPaymentStatus(reservation.getPaymentStatus());
        res.setPaymentMethod(reservation.getPaymentMethod());
        res.setTotal(reservation.getTotal());
        res.setCreatedAt(reservation.getCreatedAt());
        res.setUpdatedAt(reservation.getUpdatedAt());
        return res;
    }
}
