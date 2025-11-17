package vn.huy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.huy.controller.request.ReservationCreationRequest;
import vn.huy.controller.response.ReservationResponse;
import vn.huy.service.ReservationService;

import java.util.List;

@Slf4j(topic = "USER-CONTROLLER")
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Reservation lifecycle")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Create a reservation (customer)")
    @PostMapping
    public ReservationResponse create(ReservationCreationRequest request) {
        return reservationService.createReservation(request);
    }

    @PutMapping("/{id}")
    public ReservationResponse updateReservation(@PathVariable Long id, @RequestBody @Valid ReservationCreationRequest request) {
        return reservationService.updateReservation(id, request);
    }

    @Operation(summary = "Cancel reservation (customer or admin)")
    @DeleteMapping("/{id}")
    public ReservationResponse cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }

    @Operation(summary = "Get reservations (admin/staff can see all; user sees own)")
    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getReservations();
    }
}
