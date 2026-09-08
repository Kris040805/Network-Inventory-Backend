package com.example.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RouterFullUpdateRequest {

    @NotNull
    private Long siteId;

    @NotBlank
    @Size(max = 100)
    private String hostname;

    @NotBlank
    @Size(max = 60)
    private String vendor;

    @NotBlank
    @Size(max = 60)
    private String model;

    @NotBlank
    @Size(max = 60)
    private String serialNumber;

    @Size(max = 45)
    private String managementIp;

    @Size(max = 40)
    private String softwareVersion;

    @NotBlank
    @Pattern(regexp = "^(IN_SERVICE|MAINTENANCE|SPARE|DECOMMISSIONED)$")
    private String status;


    public RouterFullUpdateRequest() {}


    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getManagementIp() {
        return managementIp;
    }

    public void setManagementIp(String managementIp) {
        this.managementIp = managementIp;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
