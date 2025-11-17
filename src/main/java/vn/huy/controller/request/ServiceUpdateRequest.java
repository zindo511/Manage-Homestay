package vn.huy.controller.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceUpdateRequest {
    private String name;
    private Long groupId;
    private BigDecimal unitPrice;
    private Boolean isActive;
    private String description;
}
