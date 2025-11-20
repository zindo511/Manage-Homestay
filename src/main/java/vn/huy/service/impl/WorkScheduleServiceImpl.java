package vn.huy.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.huy.controller.request.CreateWorkScheduleRequest;
import vn.huy.controller.response.WorkScheduleResponse;
import vn.huy.exception.ResourceNotFoundException;
import vn.huy.model.User;
import vn.huy.model.WorkSchedule;
import vn.huy.repository.UserRepository;
import vn.huy.repository.WorkScheduleRepository;
import vn.huy.service.WorkScheduleService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;

    @Override
    public List<WorkScheduleResponse> getAllWorkSchedules() {
        List<WorkSchedule> schedules = workScheduleRepository.findAll();

        return schedules.stream()
                .map(s -> new WorkScheduleResponse(
                        s.getId(),
                        s.getEmployee().getId(),
                        s.getEmployee().getName(),
                        s.getWorkDate(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getTask(),
                        s.getStatus(),
                        s.getDescription()
                ))
                .toList();
    }

    @Override
    public WorkScheduleResponse createWorkSchedule(CreateWorkScheduleRequest request) {
        // 1. Check if employee exists
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // 2. Create work schedule
        WorkSchedule workSchedule = new WorkSchedule();
        workSchedule.setEmployee(employee);
        workSchedule.setWorkDate(request.getWorkDate());
        workSchedule.setStartTime(request.getStartTime());
        workSchedule.setEndTime(request.getEndTime());
        workSchedule.setTask(request.getTask());
        workSchedule.setStatus(request.getStatus());
        workSchedule.setDescription(request.getDescription());

        workScheduleRepository.save(workSchedule);

        return new WorkScheduleResponse(
                workSchedule.getId(),
                employee.getId(),
                employee.getName(),
                workSchedule.getWorkDate(),
                workSchedule.getStartTime(),
                workSchedule.getEndTime(),
                workSchedule.getTask(),
                workSchedule.getStatus(),
                workSchedule.getDescription()
        );
    }

    @Override
    public void deleteWorkSchedule(Long id) {
        WorkSchedule schedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work Schedule not found"));

        workScheduleRepository.delete(schedule);
    }


}
