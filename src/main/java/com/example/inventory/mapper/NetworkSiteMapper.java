package com.example.inventory.mapper;

import com.example.inventory.dto.request.SiteCreateRequest;
import com.example.inventory.dto.request.SiteFullUpdateRequest;
import com.example.inventory.dto.request.SitePartialUpdateRequest;
import com.example.inventory.dto.response.SiteResponse;
import com.example.inventory.entity.NetworkSite;
import org.springframework.stereotype.Component;

@Component
public class NetworkSiteMapper {

    public NetworkSite toEntity(SiteCreateRequest request) {
        NetworkSite site = new NetworkSite();
        site.setSiteCode(request.getSiteCode());
        site.setName(request.getName());
        site.setAddress(request.getAddress());
        site.setCity(request.getCity());
        site.setCountryCode(request.getCountryCode());
        site.setLatitude(request.getLatitude());
        site.setLongitude(request.getLongitude());
        site.setStatus(request.getStatus());

        return site;
    }

    public SiteResponse toResponse(NetworkSite site) {
        SiteResponse response = new SiteResponse();
        response.setId(site.getId());
        response.setSiteCode(site.getSiteCode());
        response.setName(site.getName());
        response.setAddress(site.getAddress());
        response.setCity(site.getCity());
        response.setCountryCode(site.getCountryCode());
        response.setLatitude(site.getLatitude());
        response.setLongitude(site.getLongitude());
        response.setStatus(site.getStatus());
        response.setCreatedAt(site.getCreatedAt());
        response.setUpdatedAt(site.getUpdatedAt());

        return response;
    }

    public void updateEntity(SiteFullUpdateRequest request, NetworkSite site) {
        site.setSiteCode(request.getSiteCode());
        site.setName(request.getName());
        site.setAddress(request.getAddress());
        site.setCity(request.getCity());
        site.setCountryCode(request.getCountryCode());
        site.setLatitude(request.getLatitude());
        site.setLongitude(request.getLongitude());
        site.setStatus(request.getStatus());
    }

    public void updateEntity(SitePartialUpdateRequest request, NetworkSite site) {
        if (request.getSiteCode() != null) {
            site.setSiteCode(request.getSiteCode());
        }
        if (request.getName() != null) {
            site.setName(request.getName());
        }
        if (request.getAddress() != null) {
            site.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            site.setCity(request.getCity());
        }
        if (request.getCountryCode() != null) {
            site.setCountryCode(request.getCountryCode());
        }
        if (request.getLatitude() != null) {
            site.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            site.setLongitude(request.getLongitude());
        }
        if (request.getStatus() != null) {
            site.setStatus(request.getStatus());
        }
    }

}
