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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.huy.common.RoomStatus;
import vn.huy.common.RoomType;
import vn.huy.controller.request.RoomCreationRequest;
import vn.huy.controller.request.RoomImageRequest;
import vn.huy.controller.request.RoomUpdateRequest;
import vn.huy.controller.response.ApiResponse;
import vn.huy.controller.response.RoomImageResponse;
import vn.huy.controller.response.RoomResponse;
import vn.huy.controller.response.RoomStatusHistoryResponse;
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
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff', 'Customer')")
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

    @Operation(summary = "Add room (admin)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomCreationRequest request){
        log.info("createRoom");
        RoomResponse response = roomService.createRoom(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", response));
    }

    @Operation(summary = "Get room by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Adnin', 'Staff', 'Customer')")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id){
        RoomResponse response = roomService.findRoomById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Room found successfully", response));
    }

    @Operation(summary = "Update room (admin, staff)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff')")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(@PathVariable @Valid @Min(value = 1, message = "id must be equals or greater than 1") Long id ,
                                   @Valid @RequestBody RoomUpdateRequest request) {
        log.info("updateRoom");
        RoomResponse response = roomService.update(id, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Room updated successfully", response));
    }

    @Operation(summary = "Delete room (admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<ApiResponse<Void>> deleteRoomById(@PathVariable @Valid @Min(value = 1, message = "id must be equals or greater than 1") Long id){
        roomService.deleteRoomById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Room deleted successfully", null));
    }

    @Operation(summary = "Get images for a room")
    @GetMapping("/{id}/images")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff', 'Customer')")
    public ResponseEntity<ApiResponse<List<RoomImageResponse>>> roomImages(@PathVariable @Valid @Min(value = 1, message = "id must be equals or greater than 1") Long id){
        List<RoomImageResponse> responses = roomService.roomImageList(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(" Get room images successfully", responses));
    }

    @Operation(summary = "Add images to room (admin, staff)")
    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff')")
    public ResponseEntity<ApiResponse<List<RoomImageResponse>>> uploadImage(@PathVariable @Valid @Min(value = 1, message = "id must be equals or greater than 1") Long id, @Valid @RequestBody RoomImageRequest imageUrl){
        List<RoomImageResponse> images = roomService.uploadImage(id, imageUrl);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room image upload successfully", images));
    }

    @Operation(summary = "Get room status history")
    @GetMapping("{id}/status-history")
    @PreAuthorize("hasAnyAuthority('Admin', 'Staff')")
    public ResponseEntity<ApiResponse<List<RoomStatusHistoryResponse>>> getRoomStatusHistory(@PathVariable Long id){
        log.info("getRoomStatusHistory");
        List<RoomStatusHistoryResponse> responses = roomService.getRoomStatusHistory(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Room status history successfully", responses));
    }
}
