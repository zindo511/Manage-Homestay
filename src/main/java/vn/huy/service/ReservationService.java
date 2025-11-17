package vn.huy.service;

import vn.huy.controller.request.ReservationCreationRequest;
import vn.huy.controller.response.ReservationResponse;
import vn.huy.model.Reservation;

import java.util.List;

public interface ReservationService {

    ReservationResponse createReservation(ReservationCreationRequest request);

    ReservationResponse updateReservation(Long id, ReservationCreationRequest request);

    ReservationResponse cancelReservation(Long id);

    List<ReservationResponse> getReservations();
}
