package vn.huy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.huy.common.WorkStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "workschedule")
public class WorkSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(name = "work_date", nullable = false)
    private LocalDateTime workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status;

    private String description;
}
