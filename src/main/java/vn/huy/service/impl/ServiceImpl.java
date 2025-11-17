package vn.huy.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import vn.huy.service.ServiceInterface;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceImpl implements ServiceInterface {

    private final ServiceRepository serviceRepository;
    private final ServiceGroupRepository serviceGroupRepository;

    @Override
    public List<ServiceResponse> getAll(Integer groupId, Boolean isActive, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("serviceInterface.getAll()");
        List<ServiceEntity> serviceEntities = serviceRepository.findByFilter(groupId, isActive, minPrice, maxPrice);
        return serviceEntities.stream()
                .map(entity ->
                        new ServiceResponse(entity.getId(), entity.getName(), entity.getGroup().getId(), entity.getUnitPrice(), entity.isActive(), entity.getDescription()))
                .toList();
    }

    @Override
    public void createService(ServiceCreationRequest request) {

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
    }

    @Override
    public void updateService(Long id, ServiceUpdateRequest request) {
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
    }

    @Override
    public ServiceResponse deleteService(Long id) {
        ServiceEntity serviceEntity = getService(id);
        serviceEntity.setActive(false);
        serviceRepository.save(serviceEntity);
        return mapToResponse(serviceEntity);
    }

    @Override
    public ServiceGroup createServiceGroup(ServiceGroupCreationRequest request) {

        if (serviceGroupRepository.existsByNameContainingIgnoreCase(request.getName())) {
            throw new InvalidDataException("Service name already exists");
        }

        ServiceGroup serviceGroup = new ServiceGroup();
        serviceGroup.setName(request.getName());

        return serviceGroupRepository.save(serviceGroup);
    }

    @Override
    public void deleteServiceGroup(Long id) {
        serviceGroupRepository.deleteById(id);
    }

    @Override
    public ServiceGroup updateServiceGroup(Long id, ServiceGroupCreationRequest request) {
        ServiceGroup serviceGroup = getServiceGroup(id);
        if (serviceGroupRepository.existsByNameContainingIgnoreCase(request.getName())) {
            throw new InvalidDataException("ServiceGroup name already exists");
        }
        serviceGroup.setName(request.getName());
        return serviceGroupRepository.save(serviceGroup);
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
