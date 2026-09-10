package com.example.inventory.mapper;

import com.example.inventory.dto.request.CardCreateRequest;
import com.example.inventory.dto.request.CardFullUpdateRequest;
import com.example.inventory.dto.request.CardPartialUpdateRequest;
import com.example.inventory.dto.response.CardResponse;
import com.example.inventory.entity.Card;
import com.example.inventory.entity.Slot;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public Card toEntity(CardCreateRequest request, Slot slot) {
        Card card = new Card();
        card.setSlot(slot);
        card.setPartNumber(request.getPartNumber());
        card.setSerialNumber(request.getSerialNumber());
        card.setCardType(request.getCardType());
        card.setPortCount(request.getPortCount());
        card.setHardwareRevision(request.getHardwareRevision());
        card.setStatus(request.getStatus());

        return card;
    }

    public CardResponse toResponse(Card card) {
        CardResponse response = new CardResponse();

        response.setId(card.getId());
        response.setSlotId(card.getSlot() != null ? card.getSlot().getId() : null);
        response.setPartNumber(card.getPartNumber());
        response.setSerialNumber(card.getSerialNumber());
        response.setCardType(card.getCardType());
        response.setPortCount(card.getPortCount());
        response.setHardwareRevision(card.getHardwareRevision());
        response.setStatus(card.getStatus());
        response.setCreatedAt(card.getCreatedAt());
        response.setUpdatedAt(card.getUpdatedAt());

        return response;
    }

    public void updateEntity(CardFullUpdateRequest request, Card card, Slot slot) {
        card.setSlot(slot);
        card.setPartNumber(request.getPartNumber());
        card.setSerialNumber(request.getSerialNumber());
        card.setCardType(request.getCardType());
        card.setPortCount(request.getPortCount());
        card.setHardwareRevision(request.getHardwareRevision());
        card.setStatus(request.getStatus());
    }

    public void updateEntity(CardPartialUpdateRequest request, Card card, Slot slot) {
        if (slot != null) {
            card.setSlot(slot);
        }
        if (request.getPartNumber() != null) {
            card.setPartNumber(request.getPartNumber());
        }
        if (request.getSerialNumber() != null) {
            card.setSerialNumber(request.getSerialNumber());
        }
        if (request.getCardType() != null) {
            card.setCardType(request.getCardType());
        }
        if (request.getPortCount() != null) {
            card.setPortCount(request.getPortCount());
        }
        if (request.getHardwareRevision() != null) {
            card.setHardwareRevision(request.getHardwareRevision());
        }
        if (request.getStatus() != null) {
            card.setStatus(request.getStatus());
        }
    }

}