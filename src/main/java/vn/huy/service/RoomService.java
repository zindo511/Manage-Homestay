package vn.huy.service;

import vn.huy.common.RoomStatus;
import vn.huy.common.RoomType;
import vn.huy.controller.request.RoomCreationRequest;
import vn.huy.controller.request.RoomUpdateRequest;
import vn.huy.controller.response.RoomImageResponse;
import vn.huy.controller.response.RoomResponse;
import vn.huy.model.Room;

import java.math.BigDecimal;
import java.util.List;

public interface RoomService {

    List<Room> filterRooms(RoomType type, RoomStatus status, Integer capacity, BigDecimal minPrice, BigDecimal maxPrice);

    RoomResponse createRoom(RoomCreationRequest room);

    RoomResponse findRoomById(Long id);

    RoomResponse update(Long id, RoomUpdateRequest request);

    void deleteRoomById(Long id);

    List<RoomImageResponse> roomImageList(Long roomId);

    String uploadImage(Long roomId, String imageUrl);
}
