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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {
    private final static Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardMapper mapper;
    private final CardRepository repository;
    private final SlotRepository slotRepository;


    public CardService(CardMapper mapper, CardRepository repository, SlotRepository slotRepository) {
        this.mapper = mapper;
        this.repository = repository;
        this.slotRepository = slotRepository;
    }

    public CardResponse create(CardCreateRequest request) {
        logger.info("Creating card with serial number: {}", request.getSerialNumber());
        if (repository.existsBySerialNumber(request.getSerialNumber())) {
            logger.warn("Cannot create card. Card with serial number {} already exists", request.getSerialNumber());
            throw new ConflictException("Card with serial number " + request.getSerialNumber() + " already exists");
        }

        Slot slot = null;

        if (request.getSlotId() != null) {
            slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> {
                        logger.warn("Slot not found with id: {}", request.getSlotId());
                        return new NotFoundException("Slot with id " + request.getSlotId() + " does not exist");
                    });

            if (repository.existsBySlotId(request.getSlotId())) {
                logger.warn("Cannot create card. Slot with id: {} already has a card", request.getSlotId());
                throw new ConflictException("Slot with id " + request.getSlotId() + " already has a card");
            }
        }

        Card card = repository.save(mapper.toEntity(request, slot));
        logger.info("Card created successfully with id: {}", card.getId());
        return mapper.toResponse(card);
    }


    public PageResponse<CardResponse> getAll(
            Pageable pageable,
            String status
    ) {

        logger.info("Fetching cards with status: {}", status);

        Page<Card> cards = repository.findAllByFilter(pageable, status);

        logger.info("Found {} cards", cards.getTotalElements());

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
        logger.info("Fetching card with id: {}", id);

        Card card = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Card not found with id: {}", id);
                    return new NotFoundException("Card with id " + id + " does not exist");
                });

        return mapper.toResponse(card);
    }


    public CardResponse update(Long id, CardFullUpdateRequest request) {
        logger.info("Updating card with id: {}", id);

        Card card = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Card not found with id: {}", id);
                    return new NotFoundException("Card with id " + id + " does not exist");
                });

        if (repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id)) {
            logger.warn("Cannot update card. Card with serial number {} already exists", request.getSerialNumber());
            throw new ConflictException("Card with serial number " + request.getSerialNumber() + " already exists");
        }

        Slot slot = null;

        if (request.getSlotId() != null) {
            slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> {
                        logger.warn("Slot not found with id: {}", request.getSlotId());
                        return new NotFoundException("Slot with id " + request.getSlotId() + " does not exist");
                    });

            if (repository.existsBySlotIdAndIdNot(request.getSlotId(), id)) {
                logger.warn("Cannot update card. Slot with id: {} already has a card", request.getSlotId());
                throw new ConflictException("Slot with id " + request.getSlotId() + " already has a card");
            }
        }

        mapper.updateEntity(request, card, slot);
        Card updated = repository.save(card);
        logger.info("Card updated successfully with id: {}", updated.getId());
        return mapper.toResponse(updated);
    }


    public CardResponse update(Long id, CardPartialUpdateRequest request) {
        logger.info("Partially updating card with id: {}", id);

        Card card = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Card not found with id: {}", id);
                    return new NotFoundException("Card with id " + id + " does not exist");
                });

        if (request.getSerialNumber() != null && repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id)) {
            logger.warn("Cannot partially update card. Card with serial number {} already exists", request.getSerialNumber());
            throw new ConflictException("Card with serial number " + request.getSerialNumber() + " already exists");
        }

        Slot slot = card.getSlot();

        if (request.getSlotId() != null) {
            slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> {
                        logger.warn("Slot not found with id: {}", request.getSlotId());
                        return new NotFoundException("Slot with id " + request.getSlotId() + " does not exist");
                    });

            if (repository.existsBySlotIdAndIdNot(request.getSlotId(), id)) {
                logger.warn("Cannot partially update card. Slot with id: {} already has a card", request.getSlotId());
                throw new ConflictException("Slot with id " + request.getSlotId() + " already has a card");
            }
        }

        mapper.updateEntity(request, card, slot);
        Card updated = repository.save(card);
        logger.info("Card partially updated successfully with id: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting card with id: {}", id);
        Card card = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Card not found with id: {}", id);
                    return new NotFoundException("Card with id " + id + " does not exist");
                });

        Slot slot = card.getSlot();

        if (slot != null) {
            slot.setCard(null);
        }

        repository.delete(card);
        logger.info("Card deleted successfully with id: {}", id);
    }

}
