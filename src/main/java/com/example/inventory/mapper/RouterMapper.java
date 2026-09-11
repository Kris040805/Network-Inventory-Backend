package com.example.inventory.mapper;

import com.example.inventory.dto.request.RouterCreateRequest;
import com.example.inventory.dto.request.RouterFullUpdateRequest;
import com.example.inventory.dto.request.RouterPartialUpdateRequest;
import com.example.inventory.dto.response.RouterResponse;
import com.example.inventory.entity.NetworkSite;
import com.example.inventory.entity.Router;
import org.springframework.stereotype.Component;

@Component
public class RouterMapper {

    public Router toEntity(RouterCreateRequest request, NetworkSite site) {
        Router router = new Router();
        router.setSite(site);
        router.setHostname(request.getHostname());
        router.setVendor(request.getVendor());
        router.setModel(request.getModel());
        router.setSerialNumber(request.getSerialNumber());
        router.setManagementIp(request.getManagementIp());
        router.setSoftwareVersion(request.getSoftwareVersion());
        router.setStatus(request.getStatus());

        return router;
    }

    public RouterResponse toResponse(Router router) {
        RouterResponse response = new RouterResponse();
        response.setId(router.getId());
        response.setSiteId(router.getSite().getId());
        response.setHostname(router.getHostname());
        response.setVendor(router.getVendor());
        response.setModel(router.getModel());
        response.setSerialNumber(router.getSerialNumber());
        response.setManagementIp(router.getManagementIp());
        response.setSoftwareVersion(router.getSoftwareVersion());
        response.setStatus(router.getStatus());
        response.setCreatedAt(router.getCreatedAt());
        response.setUpdatedAt(router.getUpdatedAt());

        return response;
    }

    public void updateEntity(RouterFullUpdateRequest request, Router router, NetworkSite site) {
        router.setSite(site);
        router.setHostname(request.getHostname());
        router.setVendor(request.getVendor());
        router.setModel(request.getModel());
        router.setSerialNumber(request.getSerialNumber());
        router.setManagementIp(request.getManagementIp());
        router.setSoftwareVersion(request.getSoftwareVersion());
        router.setStatus(request.getStatus());
    }

    public void updateEntity(RouterPartialUpdateRequest request, Router router, NetworkSite site) {
        if (request.getSiteId() != null) {
            router.setSite(site);
        }
        if (request.getHostname() != null) {
            router.setHostname(request.getHostname());
        }
        if (request.getVendor() != null) {
            router.setVendor(request.getVendor());
        }
        if (request.getModel() != null) {
            router.setModel(request.getModel());
        }
        if (request.getSerialNumber() != null) {
            router.setSerialNumber(request.getSerialNumber());
        }
        if (request.getManagementIp() != null) {
            router.setManagementIp(request.getManagementIp());
        }
        if (request.getSoftwareVersion() != null) {
            router.setSoftwareVersion(request.getSoftwareVersion());
        }
        if (request.getStatus() != null) {
            router.setStatus(request.getStatus());
        }
    }

}
