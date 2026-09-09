package com.example.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ShelfFullUpdateRequest {
    @NotNull
    private Long routerId;

    @Positive
    @NotNull
    private Integer shelfNumber;

    @Size(max = 60)
    private String shelfType;

    @Size(max = 60)
    private String serialNumber;

    @Positive
    @NotNull
    private Integer totalSlots;

    @NotBlank
    private String status;

    public ShelfFullUpdateRequest() {}

    public Long getRouterId() {
        return routerId;
    }

    public void setRouterId(Long routerId) {
        this.routerId = routerId;
    }

    public Integer getShelfNumber() {
        return shelfNumber;
    }

    public void setShelfNumber(Integer shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    public String getShelfType() {
        return shelfType;
    }

    public void setShelfType(String shelfType) {
        this.shelfType = shelfType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Integer totalSlots) {
        this.totalSlots = totalSlots;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
