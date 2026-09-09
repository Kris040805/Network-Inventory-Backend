package com.example.inventory.mapper;

import com.example.inventory.dto.request.SlotCreateRequest;
import com.example.inventory.dto.request.SlotFullUpdateRequest;
import com.example.inventory.dto.request.SlotPartialUpdateRequest;
import com.example.inventory.dto.response.SlotResponse;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import org.springframework.stereotype.Component;

@Component
public class SlotMapper {

    public Slot toEntity(SlotCreateRequest request, Shelf shelf) {
        Slot slot = new Slot();

        slot.setShelf(shelf);
        slot.setSlotNumber(request.getSlotNumber());
        slot.setSlotType(request.getSlotType());
        slot.setStatus(request.getStatus());

        return slot;
    }

    public SlotResponse toResponse(Slot slot) {
        SlotResponse response = new SlotResponse();

        response.setId(slot.getId());
        response.setShelfId(slot.getShelf().getId());
        response.setSlotNumber(slot.getSlotNumber());
        response.setSlotType(slot.getSlotType());
        response.setStatus(slot.getStatus());
        response.setCreatedAt(slot.getCreatedAt());
        response.setUpdatedAt(slot.getUpdatedAt());

        return response;
    }

    public void updateEntity(SlotFullUpdateRequest request, Slot slot, Shelf shelf) {
        slot.setShelf(shelf);
        slot.setSlotNumber(request.getSlotNumber());
        slot.setSlotType(request.getSlotType());
        slot.setStatus(request.getStatus());
    }

    public void updateEntity(SlotPartialUpdateRequest request, Slot slot, Shelf shelf) {
        if (request.getShelfId() != null) {
            slot.setShelf(shelf);
        }
        if (request.getSlotNumber() != null) {
            slot.setSlotNumber(request.getSlotNumber());
        }
        if (request.getSlotType() != null) {
            slot.setSlotType(request.getSlotType());
        }
        if (request.getStatus() != null) {
            slot.setStatus(request.getStatus());
        }
    }

}
