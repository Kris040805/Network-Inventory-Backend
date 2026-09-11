package com.example.inventory.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SlotPartialUpdateRequest {

    private Long shelfId;

    private Integer slotNumber;

    @Size(max = 60)
    private String slotType;

    @Pattern(regexp = "^(EMPTY|OCCUPIED|RESERVED|FAULTY)$")
    private String status;

    public SlotPartialUpdateRequest() {
    }


    public Long getShelfId() {
        return shelfId;
    }

    public void setShelfId(Long shelfId) {
        this.shelfId = shelfId;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
