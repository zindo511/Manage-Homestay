package vn.huy.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.huy.common.PaymentStatus;
import vn.huy.common.ReservationStatus;
import vn.huy.controller.request.AddServiceRequest;
import vn.huy.controller.request.ReservationCreationRequest;
import vn.huy.controller.request.ReservationGuestRequest;
import vn.huy.controller.response.*;
import vn.huy.exception.ResourceNotFoundException;
import vn.huy.model.*;
import vn.huy.repository.*;
import vn.huy.service.ReservationService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ReservationGuestRepository reservationGuestRepository;
    private final ReservationServiceRepository reservationServiceRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public ReservationResponse createReservation(Long userId, ReservationCreationRequest request) {
        Room room = getRoom(request.getRoomId());
        User user = getUser(userId);

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (request.getNumGuests() > room.getCapacity()) {
            throw new IllegalArgumentException("Number of guests exceeds room capacity");
        }

        // Check if the room has a duplicate schedule (status: Confirmed, Checked_in)
        boolean conflict = reservationRepository.existsByRoomIdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                room.getId(),
                List.of(ReservationStatus.Confirmed, ReservationStatus.Checked_in),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (conflict) {
            throw new IllegalArgumentException("Reservation already exists");
        }

        int nights = (int) Duration.between(request.getCheckInDate(), request.getCheckOutDate()).toDays();
        if (nights <= 0) throw new IllegalArgumentException("Reservation must be at least 1 night");
        BigDecimal roomPrice = room.getPrice();
        BigDecimal total = roomPrice.multiply(BigDecimal.valueOf(nights));

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setEmployee(null);
        reservation.setBookingDate(LocalDateTime.now());
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
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
    public Page<ReservationResponse> getReservationsPaginated(
            ReservationStatus status,
            PaymentStatus paymentStatus,
            Long userId,
            Long roomId,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());

        Page<Reservation> pageResult = reservationRepository.findByFilter(status, paymentStatus, userId, roomId, pageable);

        return pageResult.map(res -> {
            User user = getUser(res.getUser().getId());
            Room room = getRoom(res.getRoom().getId());
            return new ReservationResponse(
                    res.getId(),
                    res.getUser().getId(),
                    user != null ? user.getName() : null,
                    res.getRoom().getId(),
                    room != null ? room.getName() : null,
                    res.getEmployee().getId(),
                    res.getBookingDate(),
                    res.getCheckInDate(),
                    res.getCheckOutDate(),
                    res.getNumGuests(),
                    res.getRoomPrice(),
                    res.getStatus(),
                    res.getPaymentStatus(),
                    res.getPaymentMethod(),
                    res.getTotal()
            );
        });
    }

    @Override
    public ReservationDetailResponse getReservationById(Long id, UserPrincipal user) {
        Reservation reservation = getReservation(id);

        // If customer → can only see his own reservation
        if (user.hasRole("Customer") && !reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("You do not have permission to view this reservation");
        }

        User customer = getUser(user.getId());
        User employee = reservation.getEmployee().getId() != null
                ? getUser(reservation.getEmployee().getId())
                : null;
        Room room = getRoom(reservation.getRoom().getId());

        // Get the list of services
        List<ReservationServiceEntity> list = reservationServiceRepository.findByReservation_Id(id);
        List<ServiceItemResponse> services = list.stream().map(s -> {
            ServiceEntity service = getService(s.getService().getId());
            return new ServiceItemResponse(
                    s.getService().getId(),
                    service != null ? service.getName() : null,
                    s.getQuantity(),
                    s.getUnitPrice()
            );
        }).toList();

        // --- Get the list of guests ---
        List<ReservationGuest> guests = reservationGuestRepository.findByReservation_Id(id);

        List<ReservationGuestResponse> guestResponses = guests.stream().map(g ->
                new ReservationGuestResponse(g.getId(), g.getName(), g.getIdentityCard())
        ).toList();

        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                customer != null ? customer.getName() : null,
                reservation.getRoom().getId(),
                room != null ? room.getName() : null,
                reservation.getEmployee().getId(),
                employee != null ? employee.getName() : null,
                reservation.getBookingDate(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getNumGuests(),
                reservation.getRoomPrice(),
                reservation.getStatus(),
                reservation.getPaymentStatus(),
                reservation.getPaymentMethod(),
                reservation.getTotal(),
                services,
                guestResponses
        );
    }

    @Override
    public ReservationStatusResponse updateStatus(Long id, ReservationStatus status, UserPrincipal user) {
        Reservation reservation = getReservation(id);

        // update status
        reservation.setStatus(status);
        reservation.setUpdatedAt(LocalDateTime.now());

        reservationRepository.save(reservation);

        return new ReservationStatusResponse(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getUpdatedAt()
        );
    }

    @Override
    public List<ServiceItemResponse> getServices(Long reservationId, UserPrincipal currentUser) {
        // 1. Load booking
        Reservation reservation = getReservation(reservationId);

        // Customers can only see their own bookings.
        if (currentUser.hasRole("Customer") &&
                !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You cannot view services of another user's reservation");
        }

        // 3. Get the list from the reservation_service table
        List<ReservationServiceEntity> serviceList =
                reservationServiceRepository.findByReservation_Id(reservationId);

        // 4. Map to response
        return serviceList.stream().map(s -> {
            ServiceEntity service = getService(s.getService().getId());

            return new ServiceItemResponse(
                    s.getService().getId(),
                    service != null ? service.getName() : null,
                    s.getQuantity(),
                    s.getUnitPrice()
            );
        }).toList();
    }

    @Override
    public ServiceItemResponse addService(Long reservationId, AddServiceRequest request, UserPrincipal currentUser) {
        // 1. Load reservation
        Reservation reservation = getReservation(reservationId);

        // 2. Load service
        ServiceEntity service = getService(request.getServiceId());

        // 3. Check if the service is already in the reservation

        Optional<ReservationServiceEntity> existed =
                reservationServiceRepository.findByReservation_IdAndService_Id(reservationId, request.getServiceId());

        ReservationServiceEntity item;

        // if already exists → increase quantity
        if (existed.isPresent()) {
            // if already exists → increase quantity
            item = existed.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            // if not exist → create new
            item = new ReservationServiceEntity();
            item.setReservation(reservation);
            item.setService(service);
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(service.getUnitPrice());
        }

        reservationServiceRepository.save(item);

        // 4. Update total amount
        BigDecimal roomPrice = reservation.getRoomPrice() != null
                ? reservation.getRoomPrice()
                : BigDecimal.ZERO;
        BigDecimal serviceTotal = reservationServiceRepository.sumTotalByReservationId(reservationId);

        reservation.setTotal(roomPrice.add(serviceTotal));

        reservationRepository.save(reservation);
        // 5. Map to response
        return new ServiceItemResponse(
                service.getId(),
                service.getName(),
                item.getQuantity(),
                service.getUnitPrice()
        );
    }

    @Override
    public List<ReservationGuestResponse> getGuests(Long reservationId, UserPrincipal currentUser) {
        // 1. Load reservation
        Reservation reservation = getReservation(reservationId);

        // 2. Block customers from viewing other people's bookings
        if (currentUser.hasRole("Customer") &&
                !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You cannot view another user's guests");
        }

        // 3. Get the guest list
        List<ReservationGuest> guests =
                reservationGuestRepository.findByReservation_Id(reservationId);
        // 4. Map sang response
        return guests.stream()
                .map(g -> new ReservationGuestResponse(
                        g.getId(),
                        g.getName(),
                        g.getIdentityCard()
                ))
                .toList();
    }

    @Override
    public ReservationGuestResponse addGuest(Long reservationId, ReservationGuestRequest request, UserPrincipal currentUser) {
        // 1. Load reservation
        Reservation reservation = getReservation(reservationId);
        // 2. Customers can only add guests to their booking
        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You cannot add guests to another user's reservation");
        }

        // 3. Create guest mới
        ReservationGuest guest = new ReservationGuest();
        guest.setReservation(reservation);
        guest.setName(request.getName());
        guest.setIdentityCard(request.getIdentityCard());

        reservationGuestRepository.save(guest);

        // 4. Return response
        return new ReservationGuestResponse(
                guest.getId(),
                guest.getName(),
                guest.getIdentityCard()
        );
    }

    @Override
    public void deleteGuest(Long reservationId, Long guestId, UserPrincipal currentUser) {

        // 1. Load reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 2. Load guest
        ReservationGuest guest = reservationGuestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));

        // 3. Check guest reservation
        if (!guest.getReservation().getId().equals(reservation.getId())) {
            throw new RuntimeException("Guest does not belong to this reservation");
        }

        // 4. Customers can only delete guests from their booking.
        if (currentUser.hasRole("Customer") &&
                !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You cannot delete guest from another user's reservation");
        }

        // 5. Xóa guest
        reservationGuestRepository.delete(guest);
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

    private ServiceEntity getService(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
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
        res.setNumGuests(reservation.getNumGuests());
        res.setRoomPrice(reservation.getRoomPrice());
        res.setStatus(reservation.getStatus());
        res.setPaymentStatus(reservation.getPaymentStatus());
        res.setPaymentMethod(reservation.getPaymentMethod());
        res.setTotal(reservation.getTotal());
        return res;
    }
}
