package com.example.inventory.service;


import com.example.inventory.dto.request.CardInstallRequest;
import com.example.inventory.dto.request.SlotCreateRequest;
import com.example.inventory.dto.request.SlotFullUpdateRequest;
import com.example.inventory.dto.request.SlotPartialUpdateRequest;
import com.example.inventory.dto.response.CardResponse;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.SlotResponse;
import com.example.inventory.entity.Card;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.CardMapper;
import com.example.inventory.mapper.SlotMapper;
import com.example.inventory.repository.CardRepository;
import com.example.inventory.repository.ShelfRepository;
import com.example.inventory.repository.SlotRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlotService {
    private static final Logger logger = LoggerFactory.getLogger(SlotService.class);

    private final SlotMapper mapper;
    private final SlotRepository repository;
    private final ShelfRepository shelfRepository;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public SlotService(SlotMapper mapper, SlotRepository repository, ShelfRepository shelfRepository, CardRepository cardRepository, CardMapper cardMapper) {
        this.mapper = mapper;
        this.repository = repository;
        this.shelfRepository = shelfRepository;
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    public SlotResponse create(SlotCreateRequest request) {
        logger.info("Creating slot with number: {} for shelf with id: {}",
                request.getSlotNumber(), request.getShelfId());

        Shelf shelf = shelfRepository.findById(request.getShelfId())
                .orElseThrow(() -> {
                    logger.warn("Shelf not found with id: {}", request.getShelfId());
                    return new NotFoundException("Shelf with id " + request.getShelfId() + " does not exist");
                });

        if (repository.existsByShelfIdAndSlotNumber(request.getShelfId(), request.getSlotNumber())) {
            logger.warn("Cannot create slot. Slot number {} already exists in shelf with id: {}", request.getSlotNumber(), request.getShelfId());
            throw new ConflictException("Slot with slot number " + request.getSlotNumber() + " already exists in shelf with id " + request.getShelfId());
        }

        Slot slot = repository.save(mapper.toEntity(request, shelf));
        logger.info("Slot created successfully with id: {}", slot.getId());
        return mapper.toResponse(slot);
    }

    public PageResponse<SlotResponse> getAll(
            Pageable pageable,
            String status) {

        logger.info("Fetching slots with status: {}", status);

        Page<Slot> slots = repository.findAllByFilters(status, pageable);

        logger.info("Found {} slots", slots.getTotalElements());

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
        logger.info("Fetching slot with id: {}", id);
        Slot slot = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with id: {}", id);
                    return new NotFoundException("Slot with id " + id + " does not exist");
                });

        return mapper.toResponse(slot);
    }


    public SlotResponse update(Long id, SlotFullUpdateRequest request) {
        logger.info("Updating slot with id: {}", id);

        Slot slot = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with id: {}", id);
                    return new NotFoundException("Slot with id " + id + " does not exist");
                });

        Shelf shelf = shelfRepository.findById(request.getShelfId())
                .orElseThrow(() -> {
                    logger.warn("Shelf not found with id: {}", request.getShelfId());
                    return new NotFoundException("Shelf with id " + request.getShelfId() + " does not exist");
                });

        if (repository.existsByShelfIdAndSlotNumberAndIdNot(shelf.getId(), request.getSlotNumber(), id)) {
            logger.warn("Cannot update slot. Slot number {} already exists in shelf with id: {}", request.getSlotNumber(), shelf.getId());
            throw new ConflictException("Slot with number " + request.getSlotNumber() + " already exists in shelf with id " + shelf.getId());
        }

        mapper.updateEntity(request, slot, shelf);
        Slot updated = repository.save(slot);
        logger.info("Slot updated successfully with id: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    public SlotResponse update(Long id, SlotPartialUpdateRequest request) {
        logger.info("Partially updating slot with id: {}", id);

        Slot slot = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with id: {}", id);
                    return new NotFoundException("Slot with id " + id + " does not exist");
                });

        Shelf shelf = slot.getShelf();

        if (request.getShelfId() != null) {
            shelf = shelfRepository.findById(request.getShelfId())
                    .orElseThrow(() -> {
                        logger.warn("Shelf not found with id: {}", request.getShelfId());
                        return new NotFoundException("Shelf with id " + request.getShelfId() + " does not exist");
                    });
        }

        Integer slotNumber = request.getSlotNumber() != null ? request.getSlotNumber() : slot.getSlotNumber();

        if (request.getShelfId() != null || request.getSlotNumber() != null) {
            if (repository.existsByShelfIdAndSlotNumberAndIdNot(shelf.getId(), slotNumber, id)) {
                logger.warn("Cannot partially update slot. Slot number {} already exists in shelf with id: {}", slotNumber, shelf.getId());
                throw new ConflictException("Slot with number " + slotNumber + " already exists in shelf with id " + shelf.getId());
            }
        }

        mapper.updateEntity(request, slot, shelf);
        Slot updated = repository.save(slot);

        logger.info("Slot partially updated successfully with id: {}", updated.getId());

        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id, boolean cascade) {
        logger.info("Deleting slot with id: {} and cascade: {}", id, cascade);

        Slot slot = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with id: {}", id);
                    return new NotFoundException("Slot with id " + id + " does not exist");
                });

        if (slot.getCard() != null) {
            if (cascade) {
                cardRepository.deleteBySlotId(id);
            } else {
                logger.warn("Cannot delete slot with id: {} because it has a card and cascade=false", id);
                throw new ConflictException("Cannot delete slot with card unless cascade=true");
            }
        }

        repository.delete(slot);
        logger.info("Slot deleted successfully with id: {}", id);
    }


    public CardResponse getCard(Long id) {
        logger.info("Fetching card for slot with id: {}", id);

        Slot slot = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with id: {}", id);
                    return new NotFoundException("Slot with id " + id + " does not exist");
                });

        if (slot.getCard() == null) {
            logger.warn("Slot with id: {} does not have a card", id);
            throw new NotFoundException("Slot with id " + id + " does not have a card");
        }

        logger.info("Card found for slot with id: {}", id);
        return cardMapper.toResponse(slot.getCard());
    }


    // Using transactional so Hibernate can save the status change without slotRepository.save()
    // This is possible because slot is managed entity
    @Transactional
    public CardResponse installCard(Long id, CardInstallRequest request) {
        logger.info("Installing card with serial number: {} into slot with id: {}", request.getSerialNumber(), id);

        Slot slot = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with id: {}", id);
                    return new NotFoundException("Slot with id " + id + " does not exist");
                });

        if (cardRepository.existsBySlotId(id)) {
            logger.warn("Cannot install card. Slot with id: {} already has a card", id);
            throw new ConflictException("Slot with id " + id + " already has a card");
        }

        if (cardRepository.existsBySerialNumber(request.getSerialNumber())) {
            logger.warn("Cannot install card. Card with serial number {} already exists", request.getSerialNumber());
            throw new ConflictException("Card with serial number " + request.getSerialNumber() + " already exists");
        }

        Card card = cardRepository.save(cardMapper.toEntity(request, slot));
        slot.setStatus("OCCUPIED");
        logger.info("Card installed successfully into slot with id: {}. Card id: {}", id, card.getId());
        return cardMapper.toResponse(card);
    }


    @Transactional
    public void deleteCard(Long id) {
        logger.info("Removing card from slot with id: {}", id);

        Slot slot = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with id: {}", id);
                    return new NotFoundException("Slot with id " + id + " does not exist");
                });

        Card card = slot.getCard();

        if (card == null) {
            logger.warn("Slot with id: {} does not have a card", id);
            throw new NotFoundException("Slot with id " + id + " does not have a card");
        }

        slot.setStatus("EMPTY");
        slot.setCard(null);
        card.setSlot(null);
        cardRepository.delete(card);
        logger.info("Card removed successfully from slot with id: {}", id);
    }

}
