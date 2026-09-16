package com.example.inventory.service;

import com.example.inventory.dto.request.ShelfCreateRequest;
import com.example.inventory.dto.request.ShelfFullUpdateRequest;
import com.example.inventory.dto.request.ShelfPartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.ShelfResponse;
import com.example.inventory.dto.response.SlotResponse;
import com.example.inventory.entity.Router;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.ShelfMapper;
import com.example.inventory.mapper.SlotMapper;
import com.example.inventory.repository.CardRepository;
import com.example.inventory.repository.RouterRepository;
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
public class ShelfService {
    private static final Logger logger = LoggerFactory.getLogger(ShelfService.class);

    private final ShelfMapper mapper;
    private final ShelfRepository repository;
    private final RouterRepository routerRepository;
    private final SlotRepository slotRepository;
    private final CardRepository cardRepository;
    private final SlotMapper slotMapper;

    public ShelfService(ShelfMapper mapper, ShelfRepository repository, RouterRepository routerRepository, SlotRepository slotRepository, CardRepository cardRepository, SlotMapper slotMapper) {
        this.mapper = mapper;
        this.repository = repository;
        this.routerRepository = routerRepository;
        this.slotRepository = slotRepository;
        this.cardRepository = cardRepository;
        this.slotMapper = slotMapper;
    }

    public ShelfResponse create(ShelfCreateRequest request) {
        logger.info("Creating shelf with number: {} for router with id: {}", request.getShelfNumber(), request.getRouterId());

        Router router = routerRepository.findById(request.getRouterId())
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", request.getRouterId());
                    return new NotFoundException("Router with id " + request.getRouterId() + " does not exist");
                });

        if (repository.existsByRouterIdAndShelfNumber(router.getId(), request.getShelfNumber())) {
            logger.warn("Cannot create shelf. Shelf number {} already exists in router with id: {}", request.getShelfNumber(), router.getId());
            throw new ConflictException("Shelf with number " + request.getShelfNumber() + " already exists in router with id " + router.getId());
        }

        Shelf shelf = repository.save(mapper.toEntity(request, router));
        logger.info("Shelf created successfully with id: {}", shelf.getId());
        return mapper.toResponse(shelf);
    }


    public PageResponse<ShelfResponse> getAll(Pageable pageable, String status) {
        logger.info("Fetching shelves with status: {}", status);
        Page<Shelf> shelves = repository.findAllByFilters(status, pageable);
        logger.info("Found {} shelves", shelves.getTotalElements());
        List<ShelfResponse> items = shelves.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        PageResponse<ShelfResponse> response = new PageResponse<>(
                items,
                shelves.getNumber(),
                shelves.getSize(),
                shelves.getTotalElements(),
                shelves.getTotalPages()
        );
        return response;
    }

    public ShelfResponse getById(Long id) {
        logger.info("Fetching shelf with id: {}", id);

        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Shelf not found with id: {}", id);
                    return new NotFoundException("Shelf with id " + id + " does not exist");
                });

        return mapper.toResponse(shelf);
    }


    public ShelfResponse update(Long id, ShelfFullUpdateRequest request) {
        logger.info("Updating shelf with id: {}", id);

        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Shelf not found with id: {}", id);
                    return new NotFoundException("Shelf with id " + id + " does not exist");
                });

        Router router = routerRepository.findById(request.getRouterId())
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", request.getRouterId());
                    return new NotFoundException("Router with id " + request.getRouterId() + " does not exist");
                });

        if (repository.existsByRouterIdAndShelfNumberAndIdNot(request.getRouterId(), request.getShelfNumber(), id)) {
            logger.warn("Cannot update shelf. Shelf number {} already exists in router with id: {}", request.getShelfNumber(), router.getId());
            throw new ConflictException("Shelf with number " + request.getShelfNumber() + " already exists in router with id " + router.getId());
        }

        mapper.updateEntity(request, shelf, router);
        Shelf updated = repository.save(shelf);

        logger.info("Shelf updated successfully with id: {}", updated.getId());

        return mapper.toResponse(updated);
    }

    public ShelfResponse update(Long id, ShelfPartialUpdateRequest request) {
        logger.info("Partially updating shelf with id: {}", id);

        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Shelf not found with id: {}", id);
                    return new NotFoundException("Shelf with id " + id + " does not exist");
                });

        Router router = shelf.getRouter();

        if (request.getRouterId() != null) {
            router = routerRepository.findById(request.getRouterId())
                    .orElseThrow(() -> {
                        logger.warn("Router not found with id: {}", request.getRouterId());
                        return new NotFoundException("Router with id " + request.getRouterId() + " does not exist");
                    });
        }

        Integer shelfNumber = request.getShelfNumber() != null ? request.getShelfNumber() : shelf.getShelfNumber();

        if (request.getRouterId() != null || request.getShelfNumber() != null) {
            if (repository.existsByRouterIdAndShelfNumberAndIdNot(router.getId(), shelfNumber, id)) {
                logger.warn("Cannot partially update shelf. Shelf number {} already exists in router with id: {}", shelfNumber, router.getId());
                throw new ConflictException("Shelf with number " + shelfNumber + " already exists in router with id " + router.getId());
            }
        }

        mapper.updateEntity(request, shelf, router);
        Shelf updated = repository.save(shelf);

        logger.info("Shelf partially updated successfully with id: {}", updated.getId());

        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id, boolean cascade) {
        logger.info("Deleting shelf with id: {} and cascade: {}", id, cascade);

        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Shelf not found with id: {}", id);
                    return new NotFoundException("Shelf with id " + id + " does not exist");
                });

        if (repository.existsByIdAndSlotsIsNotEmpty(id)) {
            if (cascade) {

                List<Slot> slots = slotRepository.findByShelfId(id);
                for (Slot slot : slots) {

                    cardRepository.deleteBySlotId(slot.getId());

                }
                slotRepository.deleteByShelfId(id);

            } else {
                logger.warn("Cannot delete shelf with id: {} because it has slots and cascade=false", id);
                throw new ConflictException("Cannot delete shelf with slots unless cascade=true");
            }
        }

        repository.delete(shelf);

        logger.info("Shelf deleted successfully with id: {}", id);
    }


    public List<SlotResponse> getSlots(Long id) {
        logger.info("Fetching slots for shelf with id: {}", id);

        repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Shelf not found with id: {}", id);
                    return new NotFoundException("Shelf with id " + id + " does not exist");
                });
        List<SlotResponse> slots = slotRepository.findByShelfId(id)
                .stream()
                .map(slotMapper::toResponse)
                .toList();

        logger.info("Found {} slots for shelf with id: {}", slots.size(), id);

        return slots;
    }





}
