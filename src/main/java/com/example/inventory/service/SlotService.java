package com.example.inventory.service;


import com.example.inventory.dto.request.SlotCreateRequest;
import com.example.inventory.dto.request.SlotFullUpdateRequest;
import com.example.inventory.dto.request.SlotPartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.SlotResponse;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.SlotMapper;
import com.example.inventory.repository.CardRepository;
import com.example.inventory.repository.ShelfRepository;
import com.example.inventory.repository.SlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlotService {
    private final SlotMapper mapper;
    private final SlotRepository repository;
    private final ShelfRepository shelfRepository;
    private final CardRepository cardRepository;

    public SlotService(SlotMapper mapper, SlotRepository repository, ShelfRepository shelfRepository, CardRepository cardRepository) {
        this.mapper = mapper;
        this.repository = repository;
        this.shelfRepository = shelfRepository;
        this.cardRepository = cardRepository;
    }

    public SlotResponse create(SlotCreateRequest request) {
        Shelf shelf = shelfRepository.findById(request.getShelfId())
                .orElseThrow(() -> new NotFoundException("Shelf with id " + request.getShelfId() + " does not exist"));

        if (repository.existsByShelfIdAndSlotNumber(request.getShelfId(), request.getSlotNumber())) {
            throw new ConflictException("Slot with slot number " + request.getSlotNumber() + " already exists in shelf with id " + request.getShelfId());
        }

        Slot slot = repository.save(mapper.toEntity(request, shelf));
        return mapper.toResponse(slot);
    }

    public PageResponse<SlotResponse> getAll(
            Pageable pageable,
            String status) {

        Page<Slot> slots = repository.findAllByFilters(status, pageable);

        List<SlotResponse> items = slots.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        PageResponse<SlotResponse> response = new PageResponse<>(
                items,
                slots.getNumber(),
                slots.getSize(),
                slots.getTotalElements(),
                slots.getTotalPages()
        );

        return response;
    }


    public SlotResponse getById(Long id) {
        Slot slot = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Slot with id " + id + " does not exist"));

        return mapper.toResponse(slot);
    }


    public SlotResponse update(Long id, SlotFullUpdateRequest request) {
        Slot slot = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Slot with id " + id + " does not exist"));

        Shelf shelf = shelfRepository.findById(request.getShelfId())
                .orElseThrow(() -> new NotFoundException("Shelf with id " + request.getShelfId() + " does not exist"));

        if (repository.existsByShelfIdAndSlotNumberAndIdNot(shelf.getId(), request.getSlotNumber(), id)) {
            throw new ConflictException("Slot with number " + request.getSlotNumber() + " already exists in shelf with id " + shelf.getId());
        }

        mapper.updateEntity(request, slot, shelf);
        return mapper.toResponse(repository.save(slot));
    }

    public SlotResponse update(Long id, SlotPartialUpdateRequest request) {
        Slot slot = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Slot with id " + id + " does not exist"));

        Shelf shelf = slot.getShelf();

        if (request.getShelfId() != null) {
            shelf = shelfRepository.findById(request.getShelfId())
                    .orElseThrow(() -> new NotFoundException("Shelf with id " + request.getShelfId() + " does not exist"));

        }

        Integer slotNumber = request.getSlotNumber() != null ? request.getSlotNumber() : slot.getSlotNumber();

        if (request.getShelfId() != null || request.getSlotNumber() != null) {
            if (repository.existsByShelfIdAndSlotNumberAndIdNot(shelf.getId(), slotNumber, id)) {
                throw new ConflictException("Slot with number " + slotNumber + " already exists in shelf with id " + shelf.getId());
            }
        }

        mapper.updateEntity(request, slot, shelf);
        return mapper.toResponse(repository.save(slot));
    }

    @Transactional
    public void delete(Long id, boolean cascade) {
        Slot slot = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Slot with id " + id + " does not exist"));

        if (slot.getCard() != null) {
            if (cascade) {
                cardRepository.deleteBySlotId(id);
            } else {
                throw new ConflictException("Cannot delete slot with card unless cascade=true");
            }
        }

        repository.delete(slot);
    }

}
