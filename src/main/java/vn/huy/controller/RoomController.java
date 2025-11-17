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
import vn.huy.common.RoomStatus;
import vn.huy.common.RoomType;
import vn.huy.controller.request.RoomCreationRequest;
import vn.huy.controller.request.RoomUpdateRequest;
import vn.huy.controller.response.RoomImageResponse;
import vn.huy.controller.response.RoomResponse;
import vn.huy.model.Room;
import vn.huy.service.RoomService;

import java.math.BigDecimal;
import java.util.List;

@Slf4j(topic = "USER-CONTROLLER")
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "Get list of rooms with pagination and filters")
    @GetMapping
    public Page<RoomResponse> getRooms(
            @RequestParam(required = false) RoomType type,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ){
        log.info("getRooms - page: {}, size: {}", page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return roomService.filterRooms(type, status, capacity, minPrice, maxPrice, pageable);
    }

    @Operation(summary = "Create room (admin)")
    @PostMapping
    public RoomResponse createRoom(@Valid @RequestBody RoomCreationRequest request){
        log.info("createRoom");
        return roomService.createRoom(request);
    }

    @Operation(summary = "Get room by id")
    @GetMapping("/{id}")
    public RoomResponse getRoomById(@PathVariable Long id){
        return roomService.findRoomById(id);
    }

    @Operation(summary = "Update room (admin)")
    @PutMapping("/{id}")
    public RoomResponse updateRoom(@PathVariable @Valid @Min(value = 1, message = "id must be equals or greater than 1") Long id ,
                                   @Valid @RequestBody RoomUpdateRequest request) {
        log.info("updateRoom");
        return roomService.update(id, request);
    }

    @Operation(summary = "Delete room (admin)")
    @DeleteMapping("/{id}")
    public void deleteRoomById(@PathVariable  Long id){
        roomService.deleteRoomById(id);
    }

    @Operation(summary = "Get images for a room")
    @GetMapping("/{id}/images")
    public List<RoomImageResponse> roomImages(@PathVariable @Valid @Min(value = 1, message = "id must be equals or greater than 1") Long id){
        return roomService.roomImageList(id);
    }

    @Operation(summary = "Add images to room (admin)")
    @PostMapping("/{id}/images")
    public String uploadImage(@PathVariable Long id, @RequestBody String imageUrl){
        return roomService.uploadImage(id, imageUrl);
    }
}
