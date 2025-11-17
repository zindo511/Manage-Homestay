package vn.huy.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.huy.common.RoomStatus;
import vn.huy.common.RoomType;
import vn.huy.controller.request.RoomCreationRequest;
import vn.huy.controller.request.RoomUpdateRequest;
import vn.huy.controller.response.RoomImageResponse;
import vn.huy.controller.response.RoomResponse;
import vn.huy.exception.InvalidDataException;
import vn.huy.exception.ResourceNotFoundException;
import vn.huy.model.Room;
import vn.huy.model.RoomImage;
import vn.huy.model.RoomStatusHistory;
import vn.huy.repository.RoomImageRepository;
import vn.huy.repository.RoomRepository;
import vn.huy.repository.RoomStatusHistoryRepository;
import vn.huy.service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomStatusHistoryRepository roomStatusHistoryRepository;

    @Override
    public List<Room> filterRooms(RoomType type, RoomStatus status, Integer capacity, BigDecimal minPrice, BigDecimal maxPrice) {
        return roomRepository.findRoomsByFilters(type, status, capacity, minPrice, maxPrice);
    }

    @Transactional
    @Override
    public RoomResponse createRoom(RoomCreationRequest request) {
        if (roomRepository.existsByName(request.getName())) {
            throw new InvalidDataException("Room name already exists");
        }

        // 1) Create room
        Room room = new Room();
        room.setType(request.getType());
        room.setName(request.getName());
        room.setStatus(request.getStatus());
        room.setPrice(request.getPrice());
        room.setCapacity(request.getCapacity());
        room.setDescription(request.getDescription());
        room.setArea(request.getArea());
        roomRepository.save(room);

        // * lấy imageUrl room
        List<String> images = room.getImages().stream()
                .map(RoomImage::getImageUrl)
                .filter(Objects::nonNull)
                .filter(url -> !url.isEmpty())
                .toList();

        // 2) Sava image
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String url : request.getImages()) {
                RoomImage roomImage = new RoomImage();
                roomImage.setRoom(room);
                roomImage.setImageUrl(url);
                roomImageRepository.save(roomImage);
            }
        }

        return mapToRoomResponse(images, room);
    }

    @Override
    public RoomResponse findRoomById(Long id) {
        Room room = getRoom(id);
        List<String> images = room.getImages().stream()
                .map(RoomImage::getImageUrl)
                .filter(Objects::nonNull)
                .filter(url -> !url.isEmpty())
                .toList();
        return mapToRoomResponse(images, room);
    }

    /* =================
        UPDATE ROOM
     ================= */
    @Override
    @Transactional
    public RoomResponse update(Long id, RoomUpdateRequest request) {
        log.info("UpdateRoom");
        Room room = getRoom(id);

        // get oldImage
        List<String> oldImages = room.getImages().stream()
                .map(RoomImage::getImageUrl)
                .filter(Objects::nonNull)
                .filter(url -> !url.isEmpty())
                .toList();

        // 1. check duplicate name
        if (!room.getName().equals(request.getName()) && roomRepository.existsByName(request.getName()) && !request.getName().equals(room.getName())) {
            throw new InvalidDataException("Room name already exists");
        }

        // 2. Partial update
        if (request.getType() != null) room.setType(request.getType());
        if (request.getName() != null) room.setName(request.getName());
        if (request.getStatus() != null) room.setStatus(request.getStatus());
        if (request.getPrice() != null) room.setPrice(request.getPrice());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getArea() != null) room.setArea(request.getArea());

        roomRepository.save(room);

        // 3. Update list images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // delete old image
            roomImageRepository.deleteByRoomId(room.getId());
            for (String url : request.getImages()) {
                RoomImage roomImage = new RoomImage();
                roomImage.setRoom(room);
                roomImage.setImageUrl(url);
                roomImageRepository.save(roomImage);
            }
        }

        // 4. If status changes -> save history
        if (!room.getStatus().equals(request.getStatus())) {

            RoomStatusHistory history = new RoomStatusHistory();
            history.setRoom(room);
            history.setStatus(request.getStatus());
            history.setChangedAt(LocalDateTime.now());

            roomStatusHistoryRepository.save(history);
        }
        return mapToRoomResponse(oldImages, room);
    }

    @Override
    public void deleteRoomById(Long id) {
        log.info("deleteRoomById");
        Room room = getRoom(id);

        room.setStatus(RoomStatus.Deleted);
        room.setUpdatedAt(LocalDateTime.now());

        roomRepository.save(room);

        // Save history
        RoomStatusHistory history = new RoomStatusHistory();
        history.setRoom(room);
        history.setStatus(RoomStatus.Deleted);
        history.setChangedAt(LocalDateTime.now());

        roomStatusHistoryRepository.save(history);
    }

    @Override
    public List<RoomImageResponse> roomImageList(Long roomId) {
        Room room = getRoom(roomId);
        return room.getImages().stream()
                .map(roomImage -> new RoomImageResponse(
                        roomImage.getId(), roomId, roomImage.getImageUrl()
                )).toList();
    }

    @Override
    public String uploadImage(Long roomId, String imageUrl) {
        Room room = getRoom(roomId);

        // thêm ảnh vào database room_image
        RoomImage roomImage = new RoomImage();
        roomImage.setRoom(room);
        roomImage.setImageUrl(imageUrl);

        roomImageRepository.save(roomImage);

        // thêm ảnh vào phòng
        room.addImage(roomImage);

        return "Image uploaded successfully";
    }

    /* ======================
           HELPER
       ====================== */
    private Room getRoom(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    private RoomResponse mapToRoomResponse(List<String> images, Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .type(room.getType().toString())
                .name(room.getName())
                .status(room.getStatus().toString())
                .price(room.getPrice())
                .capacity(room.getCapacity())
                .description(room.getDescription())
                .area(room.getArea())
                .createdAt(room.getCreatedAt())
                .images(images)
                .build();
    }
}
