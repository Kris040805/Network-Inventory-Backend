package com.example.inventory.service;

import com.example.inventory.dto.request.CardCreateRequest;
import com.example.inventory.dto.request.CardFullUpdateRequest;
import com.example.inventory.dto.request.CardPartialUpdateRequest;
import com.example.inventory.dto.response.CardResponse;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.entity.Card;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.CardMapper;
import com.example.inventory.repository.CardRepository;
import com.example.inventory.repository.SlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {
    private final CardMapper mapper;
    private final CardRepository repository;
    private final SlotRepository slotRepository;


    public CardService(CardMapper mapper, CardRepository repository, SlotRepository slotRepository) {
        this.mapper = mapper;
        this.repository = repository;
        this.slotRepository = slotRepository;
    }

    public CardResponse create(CardCreateRequest request) {
        if (repository.existsBySerialNumber(request.getSerialNumber())) {
            throw new ConflictException("Card with serial number " + request.getSerialNumber() + " already exists");
        }

        Slot slot = null;

        if (request.getSlotId() != null) {
            slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new NotFoundException("Slot with id " + request.getSlotId() + " does not exist"));

            if (repository.existsBySlotId(request.getSlotId())) {
                throw new ConflictException("Slot with id " + request.getSlotId() + " already has a card");
            }
        }

        Card card = repository.save(mapper.toEntity(request, slot));
        return mapper.toResponse(card);
    }


    public PageResponse<CardResponse> getAll(
            Pageable pageable,
            String status
    ) {

        Page<Card> cards = repository.findAllByFilter(pageable, status);

        List<CardResponse> items = cards.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        PageResponse<CardResponse> response = new PageResponse<>(
                items,
                cards.getNumber(),
                cards.getSize(),
                cards.getTotalElements(),
                cards.getTotalPages()
        );

        return response;
    }


    public CardResponse getById(Long id) {
        Card card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card with id " + id + " does not exist"));

        return mapper.toResponse(card);
    }


    public CardResponse update(Long id, CardFullUpdateRequest request) {
        Card card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card with id " + id + " does not exist"));

        if (repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id)) {
            throw new ConflictException("Card with serial number " + request.getSerialNumber() + " already exists");
        }

        Slot slot = null;

        if (request.getSlotId() != null) {
            slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new NotFoundException("Slot with id " + request.getSlotId() + " does not exist"));

            if (repository.existsBySlotIdAndIdNot(request.getSlotId(), id)) {
                throw new ConflictException("Slot with id " + request.getSlotId() + " already has a card");
            }
        }

        mapper.updateEntity(request, card, slot);
        return mapper.toResponse(repository.save(card));
    }


    public CardResponse update(Long id, CardPartialUpdateRequest request) {
        Card card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card with id " + id + " does not exist"));

        if (request.getSerialNumber() != null && repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id)) {
            throw new ConflictException("Card with serial number " + request.getSerialNumber() + " already exists");
        }

        Slot slot = card.getSlot();

        if (request.getSlotId() != null) {
            slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new NotFoundException("Slot with id " + request.getSlotId() + " does not exist"));

            if (repository.existsBySlotIdAndIdNot(request.getSlotId(), id)) {
                throw new ConflictException("Slot with id " + request.getSlotId() + " already has a card");
            }
        }

        mapper.updateEntity(request, card, slot);
        return mapper.toResponse(repository.save(card));
    }

    @Transactional
    public void delete(Long id) {
        Card card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card with id " + id + " does not exist"));

        Slot slot = card.getSlot();

        if (slot != null) {
            slot.setCard(null);
        }

        repository.delete(card);
    }

}
