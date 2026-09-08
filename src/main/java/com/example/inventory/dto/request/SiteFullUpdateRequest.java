package com.example.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class SiteFullUpdateRequest {
    @NotBlank
    @Size(max = 20)
    private String siteCode;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 255)
    private String address;

    @Size(max = 80)
    private String city;

    @Pattern(regexp = "^[A-Z]{2}$")
    private String countryCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @NotBlank
    @Pattern(regexp = "^(ACTIVE|PLANNED|DECOMMISSIONED)$")
    private String status;



    // CONSTRUCTORS

    public SiteFullUpdateRequest() {}



    //Getters-Setters


    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
