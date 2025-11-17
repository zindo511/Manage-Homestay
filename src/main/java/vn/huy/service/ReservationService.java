package vn.huy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.huy.controller.request.ReservationCreationRequest;
import vn.huy.controller.response.ReservationResponse;


public interface ReservationService {

    ReservationResponse createReservation(ReservationCreationRequest request);

    ReservationResponse updateReservation(Long id, ReservationCreationRequest request);

    ReservationResponse cancelReservation(Long id);

    Page<ReservationResponse> getReservationsPaginated(Pageable pageable);

    ReservationResponse getReservationById(Long id);
}
