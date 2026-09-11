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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShelfService {
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
        Router router = routerRepository.findById(request.getRouterId())
                .orElseThrow(() -> new NotFoundException("Router with id " + request.getRouterId() + " does not exist"));

        if (repository.existsByRouterIdAndShelfNumber(router.getId(), request.getShelfNumber())) {
            throw new ConflictException("Shelf with number " + request.getShelfNumber() + " already exists in router with id " + router.getId());
        }

        Shelf shelf = repository.save(mapper.toEntity(request, router));
        return mapper.toResponse(shelf);
    }


    public PageResponse<ShelfResponse> getAll(Pageable pageable, String status) {
        Page<Shelf> shelves = repository.findAllByFilters(status, pageable);
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
        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shelf with id " + id + " does not exist"));

        return mapper.toResponse(shelf);
    }


    public ShelfResponse update(Long id, ShelfFullUpdateRequest request) {
        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shelf with id " + id + " does not exist"));

        Router router = routerRepository.findById(request.getRouterId())
                .orElseThrow(() -> new NotFoundException("Router with id " + request.getRouterId() + " does not exist"));

        if (repository.existsByRouterIdAndShelfNumberAndIdNot(request.getRouterId(), request.getShelfNumber(), id)) {
            throw new ConflictException("Shelf with number " + request.getShelfNumber() + " already exists in router with id " + router.getId());
        }

        mapper.updateEntity(request, shelf, router);
        Shelf updated = repository.save(shelf);
        return mapper.toResponse(updated);
    }

    public ShelfResponse update(Long id, ShelfPartialUpdateRequest request) {
        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shelf with id " + id + " does not exist"));

        Router router = shelf.getRouter();

        if (request.getRouterId() != null) {
            router = routerRepository.findById(request.getRouterId())
                .orElseThrow(() -> new NotFoundException("Router with id " + request.getRouterId() + " does not exist"));
        }

        Integer shelfNumber = request.getShelfNumber() != null ? request.getShelfNumber() : shelf.getShelfNumber();

        if (request.getRouterId() != null || request.getShelfNumber() != null) {
            if (repository.existsByRouterIdAndShelfNumberAndIdNot(router.getId(), shelfNumber, id)) {
                throw new ConflictException("Shelf with number " + shelfNumber + " already exists in router with id " + router.getId());
            }
        }

        mapper.updateEntity(request, shelf, router);
        Shelf updated = repository.save(shelf);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id, boolean cascade) {
        Shelf shelf = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shelf with id " + id + " does not exist"));

        if (repository.existsByIdAndSlotsIsNotEmpty(id)) {
            if (cascade) {

                List<Slot> slots = slotRepository.findByShelfId(id);
                for (Slot slot : slots) {

                    cardRepository.deleteBySlotId(slot.getId());

                }
                slotRepository.deleteByShelfId(id);

            } else {
                throw new ConflictException("Cannot delete shelf with slots unless cascade=true");
            }
        }

        repository.delete(shelf);
    }


    public List<SlotResponse> getSlots(Long id) {
        repository.findById(id).orElseThrow(() -> new NotFoundException("Shelf with id " + id + " does not exist"));
        List<SlotResponse> slots = slotRepository.findByShelfId(id).stream().map(slotMapper::toResponse).toList();
        return slots;
    }





}
