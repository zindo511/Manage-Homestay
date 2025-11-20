package vn.huy.service;

import vn.huy.controller.request.CreateWorkScheduleRequest;
import vn.huy.controller.response.WorkScheduleResponse;

import java.util.List;

public interface WorkScheduleService {

    List<WorkScheduleResponse> getAllWorkSchedules();

    WorkScheduleResponse createWorkSchedule(CreateWorkScheduleRequest request);

    void deleteWorkSchedule(Long id);
}
