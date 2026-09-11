package com.example.inventory.dto.request;


import jakarta.validation.constraints.*;


public class CardCreateRequest {

    private Long slotId;

    @NotBlank
    @Size(max = 60)
    private String partNumber;

    @NotBlank
    @Size(max = 60)
    private String serialNumber;

    @NotBlank
    @Size(max = 40)
    private String cardType;

    @PositiveOrZero
    private Integer portCount;

    @Size(max = 20)
    private String hardwareRevision;

    @NotBlank
    @Pattern(regexp = "^(INSTALLED|IN_STOCK|FAULTY|RETIRED)$")
    private String status;


    public CardCreateRequest() {
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Integer getPortCount() {
        return portCount;
    }

    public void setPortCount(Integer portCount) {
        this.portCount = portCount;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public void setHardwareRevision(String hardwareRevision) {
        this.hardwareRevision = hardwareRevision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
