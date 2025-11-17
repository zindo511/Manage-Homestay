package vn.huy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import vn.huy.controller.request.ReservationCreationRequest;
import vn.huy.controller.response.ReservationResponse;
import vn.huy.service.ReservationService;


@Slf4j(topic = "USER-CONTROLLER")
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Reservation lifecycle")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Create a reservation (customer)")
    @PostMapping
    public ReservationResponse create(@RequestBody @Valid ReservationCreationRequest request) {
        return reservationService.createReservation(request);
    }

    @Operation(summary = "Update reservation")
    @PutMapping("/{id}")
    public ReservationResponse updateReservation(@PathVariable @Min(1) Long id, @RequestBody @Valid ReservationCreationRequest request) {
        return reservationService.updateReservation(id, request);
    }

    @Operation(summary = "Cancel reservation (customer or admin)")
    @DeleteMapping("/{id}")
    public ReservationResponse cancelReservation(@PathVariable @Min(1) Long id) {
        return reservationService.cancelReservation(id);
    }

    @Operation(summary = "Get all reservations with pagination")
    @GetMapping
    public Page<ReservationResponse> getAllReservations(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return reservationService.getReservationsPaginated(pageable);
    }

    @Operation(summary = "Get reservation by ID")
    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable @Min(1) Long id) {
        return reservationService.getReservationById(id);
    }
}
