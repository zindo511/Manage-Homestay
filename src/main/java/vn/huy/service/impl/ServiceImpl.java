package vn.huy.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.huy.controller.request.ServiceCreationRequest;
import vn.huy.controller.request.ServiceGroupCreationRequest;
import vn.huy.controller.request.ServiceUpdateRequest;
import vn.huy.controller.response.ServiceResponse;
import vn.huy.exception.InvalidDataException;
import vn.huy.exception.ResourceNotFoundException;
import vn.huy.model.ServiceEntity;
import vn.huy.model.ServiceGroup;
import vn.huy.repository.ServiceGroupRepository;
import vn.huy.repository.ServiceRepository;
import vn.huy.service.ServiceService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceGroupRepository serviceGroupRepository;

    @Override
    public Page<ServiceResponse> getAllPaginated(Integer groupId, Boolean isActive,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 Pageable pageable) {
        log.info("Getting services with pagination - page: {}", pageable.getPageNumber());
        Page<ServiceEntity> page = serviceRepository.findByFilter(groupId, isActive, minPrice, maxPrice, pageable);
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ServiceResponse createService(ServiceCreationRequest request) {

        // check duplicate name in group
        if (serviceRepository.existsByNameAndGroup_Id(request.getName(), request.getGroupId())) {
            throw new InvalidDataException("Service already exists");
        }

        ServiceGroup serviceGroup = serviceGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("groupId not found"));

        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setName(request.getName());
        serviceEntity.setGroup(serviceGroup);
        serviceEntity.setUnitPrice(request.getUnitPrice());
        serviceEntity.setActive(request.getIsActive());
        serviceEntity.setDescription(request.getDescription());

        serviceRepository.save(serviceEntity);

        return mapToResponse(serviceEntity);
    }

    @Override
    public List<ServiceGroup> getAllServiceGroups() {
        return serviceGroupRepository.findAll();
    }

    @Override
    public ServiceGroup addServiceGroup(ServiceGroupCreationRequest request) {
        ServiceGroup serviceGroup = new ServiceGroup();
        serviceGroup.setName(request.getName());
        serviceGroupRepository.save(serviceGroup);
        return serviceGroup;
    }

    @Override
    @Transactional
    public ServiceResponse updateService(Long id, ServiceUpdateRequest request) {
        log.info("serviceInterface.update()");
        ServiceEntity entity = getService(id);

        // check duplicate name + group
        if (request.getName() != null && request.getGroupId() != null) {
            if (serviceRepository.existsByNameAndGroup_Id(request.getName(), request.getGroupId()) &&
                    (!request.getName().equals(entity.getName()) || !request.getGroupId().equals(entity.getGroup().getId()))) {
                throw new InvalidDataException("Service name already exists in this group");
            }
        }

        // Partial update
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getGroupId() != null) entity.setGroup(getServiceGroup(request.getGroupId()));
        if (request.getUnitPrice() != null) entity.setUnitPrice(request.getUnitPrice());
        if (request.getIsActive() != null) entity.setActive(request.getIsActive());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());

        serviceRepository.save(entity);

        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public ServiceResponse deleteService(Long id) {
        ServiceEntity serviceEntity = getService(id);
        if (!serviceEntity.isActive()) {
            throw new InvalidDataException("Service is already inactive");
        }
        serviceEntity.setActive(false);
        serviceRepository.save(serviceEntity);
        return mapToResponse(serviceEntity);
    }

    @Override
    @Transactional
    public void deleteServiceGroup(Long id) {
        ServiceGroup group = getServiceGroup(id);

        boolean hasServices = serviceRepository.existsByGroup_Id(id);
        if (hasServices) {
            throw new InvalidDataException("Cannot delete group because there is still a linked service");
        }

        serviceGroupRepository.delete(group);
    }

    /* ==========
        HELPER
    ========== */
    private ServiceEntity getService(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    private ServiceGroup getServiceGroup(Long id) {
        return serviceGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service group not found"));

    }

    private ServiceResponse mapToResponse(ServiceEntity entity) {
        return ServiceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .groupId(entity.getGroup().getId())
                .unitPrice(entity.getUnitPrice())
                .isActive(entity.isActive())
                .description(entity.getDescription())
                .build();
    }
}
