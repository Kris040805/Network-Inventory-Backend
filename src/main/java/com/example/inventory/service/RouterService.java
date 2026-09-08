package com.example.inventory.service;

import com.example.inventory.dto.request.RouterCreateRequest;
import com.example.inventory.dto.request.RouterFullUpdateRequest;
import com.example.inventory.dto.request.RouterPartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.RouterResponse;
import com.example.inventory.entity.NetworkSite;
import com.example.inventory.entity.Router;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.RouterMapper;
import com.example.inventory.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouterService {
    private final RouterMapper mapper;
    private final RouterRepository repository;
    private final NetworkSiteRepository networkSiteRepository;
    private final ShelfRepository shelfRepository;
    private final SlotRepository slotRepository;
    private final CardRepository cardRepository;

    public RouterService(RouterMapper mapper, RouterRepository repository, NetworkSiteRepository networkSiteRepository, ShelfRepository shelfRepository, SlotRepository slotRepository, CardRepository cardRepository) {
        this.mapper = mapper;
        this.repository = repository;
        this.networkSiteRepository = networkSiteRepository;
        this.shelfRepository = shelfRepository;
        this.slotRepository = slotRepository;
        this.cardRepository = cardRepository;
    }

    public RouterResponse create(RouterCreateRequest request) {
        NetworkSite site = networkSiteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new NotFoundException("Site with id " + request.getSiteId() + " does not exist"));

        if (repository.existsByHostnameOrSerialNumber(request.getHostname(), request.getSerialNumber())) {
            throw new ConflictException("Router with hostname or serial number already exists");
        }

        Router router = mapper.toEntity(request, site);
        return mapper.toResponse(repository.save(router));
    }


    public PageResponse<RouterResponse> getAll(
            Pageable pageable,
            String status
        ) {

        Page<Router> routers = repository.findAllByFilters(status, pageable);
        List<RouterResponse> items = routers.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        PageResponse<RouterResponse> response = new PageResponse<>(
                items,
                routers.getNumber(),
                routers.getSize(),
                routers.getTotalElements(),
                routers.getTotalPages()
        );
        return response;
    }

    public RouterResponse getById(Long id) {
        Router router = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Router with id " + id + " does not exist"));

        return mapper.toResponse(router);
    }


    public RouterResponse update(Long id, RouterFullUpdateRequest request) {
        Router router = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Router with id " + id + " does not exist"));

        NetworkSite site = networkSiteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new NotFoundException("Site with id " + request.getSiteId() + " does not exist"));

        if (repository.existsByHostnameAndIdNot(request.getHostname(), id)
                || repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id)) {

            throw new ConflictException(
                    "Router with hostname " + request.getHostname()
                            + " or serial number " + request.getSerialNumber()
                            + " already exists"
            );
        }

        mapper.updateEntity(request, router, site);
        Router updated = repository.save(router);
        return mapper.toResponse(updated);
    }


    public RouterResponse update(Long id, RouterPartialUpdateRequest request) {
        Router router = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Router with id " + id + " does not exist"));

        NetworkSite site = null;

        if (request.getSiteId() != null) {
            site = networkSiteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new NotFoundException(
                            "Site with id " + request.getSiteId() + " does not exist"));

        }

        if ((request.getHostname() != null && repository.existsByHostnameAndIdNot(request.getHostname(), id)) ||
                (request.getSerialNumber() != null && repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id))) {
            throw new ConflictException(
                    "Router with hostname " + request.getHostname()
                            + " or serial number " + request.getSerialNumber()
                            + " already exists"
            );
        }

        mapper.updateEntity(request, router, site);
        Router updated = repository.save(router);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id, boolean cascade) {
        Router router = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Router with id " + id + " does not exist"));

        if (repository.existsByIdAndShelvesIsNotEmpty(id)) {
            if (cascade) {

                List<Shelf> shelves = shelfRepository.findByRouterId(id);
                for (Shelf shelf : shelves) {

                    List<Slot> slots = slotRepository.findByShelfId(shelf.getId());
                    for (Slot slot : slots) {
                        cardRepository.deleteBySlotId(slot.getId());
                    }
                    slotRepository.deleteByShelfId(shelf.getId());

                }
                shelfRepository.deleteByRouterId(id);

            } else {
                throw new ConflictException("Cannot delete router with shelves unless cascade=true");
            }
        }

        repository.delete(router);
    }


}
