package vn.huy.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.huy.common.WorkStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkScheduleResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String task;
    private WorkStatus status;
    private String description;
}
