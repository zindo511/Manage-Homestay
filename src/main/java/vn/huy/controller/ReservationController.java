package vn.huy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.huy.common.PaymentStatus;
import vn.huy.common.ReservationStatus;
import vn.huy.controller.request.AddServiceRequest;
import vn.huy.controller.request.ReservationCreationRequest;
import vn.huy.controller.request.ReservationGuestRequest;
import vn.huy.controller.response.*;
import vn.huy.model.UserPrincipal;
import vn.huy.service.ReservationService;

import java.util.List;


@Slf4j(topic = "RESERVATION-CONTROLLER")
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Reservation lifecycle")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Create a reservation (customer)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('Customer')")
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody @Valid ReservationCreationRequest request) {
        ReservationResponse response = reservationService.createReservation(user.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation created successfully", response));
    }

    @Operation(summary = "Update reservation (Admin, Staff, Customer)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff', 'Customer')")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(@PathVariable @Min(1) Long id, @RequestBody @Valid ReservationCreationRequest request) {
        ReservationResponse response = reservationService.updateReservation(id, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Reservation updated successfully", response));
    }

    @Operation(summary = "Cancel reservation (customer or admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff')")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable @Min(1) Long id) {
        ReservationResponse response = reservationService.cancelReservation(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Reservation cancelled successfully", response));
    }

    @Operation(summary = "Get all reservations (Admin, Staff)")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff')")
    public Page<ReservationResponse> getAllReservations(
            @RequestParam(required = false)ReservationStatus status,
            @RequestParam(required = false)PaymentStatus paymentStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "0")@Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {

        return reservationService.getReservationsPaginated(status, paymentStatus, userId, roomId, page, size);

    }

    @Operation(summary = "Get reservation by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff', 'Customer')")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservationById(
            @PathVariable @Min(1) Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        ReservationDetailResponse response = reservationService.getReservationById(id, user);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Reservation detail successfully", response));
    }

    @Operation(summary = "Update reservation status (Admin, Staff)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff')")
    public ResponseEntity<ApiResponse<ReservationStatusResponse>> updateReservationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatus status,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        ReservationStatusResponse response = reservationService.updateStatus(id, status, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Update reservation status successfully", response));
    }

    @Operation(summary = "List of services in booking (Admin, Staff, Customer)")
    @GetMapping("/{id}/services")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff', 'Customer')")
    public ResponseEntity<ApiResponse<List<ServiceItemResponse>>> getServices(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<ServiceItemResponse> services = reservationService.getServices(id, currentUser);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Service list successfully", services));
    }

    @Operation(summary = "Add services to your booking (Admin, Staff)")
    @PostMapping("/{id}/services")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff')")
    public ResponseEntity<ApiResponse<ServiceItemResponse>> addServiceToReservation(
            @PathVariable Long id,
            @Valid @RequestBody AddServiceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ServiceItemResponse response = reservationService.addService(id, request, currentUser);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Add service successfully", response));
    }

    @Operation(summary = " (List of accompanying guests Admin, Staff, Customer)")
    @GetMapping("/{id}/guests")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff', 'Customer')")
    public ResponseEntity<ApiResponse<List<ReservationGuestResponse>>> getGuests(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<ReservationGuestResponse> guests = reservationService.getGuests(id, currentUser);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Guest list successfully", guests));
    }

    @Operation(summary = "Add guests along (Customer)")
    @PostMapping("/{id}/guests")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<ApiResponse<ReservationGuestResponse>> addGuest(
            @PathVariable Long id,
            @Valid @RequestBody ReservationGuestRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        ReservationGuestResponse response = reservationService.addGuest(id, request, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Add guest successfully", response));
    }

    @Operation(summary = "Delete accompanying guests (Customer, Admin, Staff)")
    @DeleteMapping("/{id}/guests/{guestId}")
    @PreAuthorize("hasAnyAuthority('Customer', 'Admin', 'Staff')")
    public ResponseEntity<ApiResponse<Void>> deleteGuest(
            @PathVariable Long id,
            @PathVariable Long guestId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        reservationService.deleteGuest(id, guestId, currentUser);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Delete guest successfully", null));
    }
}
