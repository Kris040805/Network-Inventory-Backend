package com.example.inventory.mapper;

import com.example.inventory.dto.request.ShelfCreateRequest;
import com.example.inventory.dto.request.ShelfFullUpdateRequest;
import com.example.inventory.dto.request.ShelfPartialUpdateRequest;
import com.example.inventory.dto.response.ShelfResponse;
import com.example.inventory.entity.Router;
import com.example.inventory.entity.Shelf;
import org.springframework.stereotype.Component;

@Component
public class ShelfMapper {
    public Shelf toEntity(ShelfCreateRequest request, Router router) {
        Shelf shelf = new Shelf();
        shelf.setRouter(router);
        shelf.setShelfNumber(request.getShelfNumber());
        shelf.setShelfType(request.getShelfType());
        shelf.setSerialNumber(request.getSerialNumber());
        shelf.setTotalSlots(request.getTotalSlots());
        shelf.setStatus(request.getStatus());

        return shelf;
    }

    public ShelfResponse toResponse(Shelf shelf) {
        ShelfResponse response = new ShelfResponse();
        response.setId(shelf.getId());
        response.setRouterId(shelf.getRouter().getId());
        response.setShelfNumber(shelf.getShelfNumber());
        response.setShelfType(shelf.getShelfType());
        response.setSerialNumber(shelf.getSerialNumber());
        response.setTotalSlots(shelf.getTotalSlots());
        response.setStatus(shelf.getStatus());
        response.setCreatedAt(shelf.getCreatedAt());
        response.setUpdatedAt(shelf.getUpdatedAt());

        return response;
    }

    public void updateEntity(ShelfFullUpdateRequest request, Shelf shelf, Router router) {
        shelf.setRouter(router);
        shelf.setShelfNumber(request.getShelfNumber());
        shelf.setShelfType(request.getShelfType());
        shelf.setSerialNumber(request.getSerialNumber());
        shelf.setTotalSlots(request.getTotalSlots());
        shelf.setStatus(request.getStatus());
    }

    public void updateEntity(ShelfPartialUpdateRequest request, Shelf shelf, Router router) {
        if (request.getRouterId() != null) {
            shelf.setRouter(router);
        }

        if (request.getShelfNumber() != null) {
            shelf.setShelfNumber(request.getShelfNumber());
        }

        if (request.getShelfType() != null) {
            shelf.setShelfType(request.getShelfType());
        }

        if (request.getSerialNumber() != null) {
            shelf.setSerialNumber(request.getSerialNumber());
        }

        if (request.getTotalSlots() != null) {
            shelf.setTotalSlots(request.getTotalSlots());
        }

        if (request.getStatus() != null) {
            shelf.setStatus(request.getStatus());
        }

    }

}
