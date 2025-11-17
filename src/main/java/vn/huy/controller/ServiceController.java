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
import org.springframework.web.bind.annotation.*;
import vn.huy.controller.request.ServiceCreationRequest;
import vn.huy.controller.request.ServiceGroupCreationRequest;
import vn.huy.controller.request.ServiceUpdateRequest;
import vn.huy.controller.response.ServiceResponse;
import vn.huy.model.ServiceGroup;
import vn.huy.service.ServiceService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/services")
@Slf4j(topic = "SERVICE-CONTROLLER")
@Tag(name = "Services", description = "Extra Services and groups")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceInterface;

    @Operation(summary = "Get all services by filters")
    @GetMapping
    public Page<ServiceResponse> getAll(
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return serviceInterface.getAllPaginated(groupId, isActive, minPrice, maxPrice, pageable);
    }

    @Operation(summary = "Create service (admin)")
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid ServiceCreationRequest request) {
        serviceInterface.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Update service desired fields", description = "Duplicate names in the same group will not be updated.")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid ServiceUpdateRequest request) {
        serviceInterface.updateService(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Soft delete, change status to false")
    @DeleteMapping("{id}")
    public ServiceResponse delete(@PathVariable Long id) {
        return serviceInterface.deleteService(id);
    }

    @Operation(summary = "Add service group")
    @PostMapping("/service-group")
    public ServiceGroup createServiceGroup(@RequestBody @Valid ServiceGroupCreationRequest request) {
        return serviceInterface.createServiceGroup(request);
    }

    @Operation(summary = "Delete service group")
    @DeleteMapping("/service-group")
    public ResponseEntity<Void> deleteServiceGroup(Long id) {
        serviceInterface.deleteServiceGroup(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Update service group", description = "Duplicate names will not be updated.")
    @PutMapping("/service-groupS/{id}")
    public ServiceGroup updateServiceGroup(@PathVariable Long id, @RequestBody @Valid ServiceGroupCreationRequest request) {
        return serviceInterface.updateServiceGroup(id, request);
    }
}
