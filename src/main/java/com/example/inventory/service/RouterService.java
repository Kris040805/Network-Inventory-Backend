package com.example.inventory.service;

import com.example.inventory.dto.request.RouterCreateRequest;
import com.example.inventory.dto.request.RouterFullUpdateRequest;
import com.example.inventory.dto.request.RouterPartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.RouterResponse;
import com.example.inventory.dto.response.RouterTreeResponse;
import com.example.inventory.dto.response.ShelfResponse;
import com.example.inventory.entity.NetworkSite;
import com.example.inventory.entity.Router;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.RouterMapper;
import com.example.inventory.mapper.ShelfMapper;
import com.example.inventory.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouterService {
    private static final Logger logger = LoggerFactory.getLogger(RouterService.class);

    private final RouterMapper mapper;
    private final RouterRepository repository;
    private final NetworkSiteRepository networkSiteRepository;
    private final ShelfRepository shelfRepository;
    private final SlotRepository slotRepository;
    private final CardRepository cardRepository;
    private final ShelfMapper shelfMapper;

    public RouterService(RouterMapper mapper, RouterRepository repository, NetworkSiteRepository networkSiteRepository, ShelfRepository shelfRepository, SlotRepository slotRepository, CardRepository cardRepository, ShelfMapper shelfMapper) {
        this.mapper = mapper;
        this.repository = repository;
        this.networkSiteRepository = networkSiteRepository;
        this.shelfRepository = shelfRepository;
        this.slotRepository = slotRepository;
        this.cardRepository = cardRepository;
        this.shelfMapper = shelfMapper;
    }

    public RouterResponse create(RouterCreateRequest request) {
        logger.info("Creating router with hostname: {} and serial number: {}",
                request.getHostname(), request.getSerialNumber());

        NetworkSite site = networkSiteRepository.findById(request.getSiteId())
                .orElseThrow(() -> {
                    logger.warn("Network site not found with id: {}", request.getSiteId());
                    return new NotFoundException("Site with id " + request.getSiteId() + " does not exist");
                });

        if (repository.existsByHostnameOrSerialNumber(request.getHostname(), request.getSerialNumber())) {
            logger.warn("Cannot create router. Hostname or serial number already exists");
            throw new ConflictException("Router with hostname or serial number already exists");
        }

        Router router = mapper.toEntity(request, site);
        Router saved = repository.save(router);

        logger.info("Router created successfully with id: {}", saved.getId());

        return mapper.toResponse(saved);
    }


    public PageResponse<RouterResponse> getAll(Pageable pageable, String status) {

        logger.info("Fetching routers with status: {}", status);
        Page<Router> routers = repository.findAllByFilters(status, pageable);
        logger.info("Found {} routers", routers.getTotalElements());
        List<RouterResponse> items = routers.getContent().stream().map(mapper::toResponse).toList();

        PageResponse<RouterResponse> response = new PageResponse<>(items, routers.getNumber(), routers.getSize(), routers.getTotalElements(), routers.getTotalPages());
        return response;
    }

    public RouterResponse getById(Long id) {
        logger.info("Fetching router with id: {}", id);

        Router router = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", id);
                    return new NotFoundException("Router with id " + id + " does not exist");
                });

        return mapper.toResponse(router);
    }


    public RouterResponse update(Long id, RouterFullUpdateRequest request) {
        logger.info("Updating router with id: {}", id);

        Router router = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", id);
                    return new NotFoundException("Router with id " + id + " does not exist");
                });

        NetworkSite site = networkSiteRepository.findById(request.getSiteId())
                .orElseThrow(() -> {
                    logger.warn("Network site not found with id: {}", request.getSiteId());
                    return new NotFoundException("Site with id " + request.getSiteId() + " does not exist");
                });

        if (repository.existsByHostnameAndIdNot(request.getHostname(), id) || repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id)) {
            logger.warn("Cannot update router. Hostname or serial number already exists");
            throw new ConflictException("Router with hostname " + request.getHostname() + " or serial number " + request.getSerialNumber() + " already exists");
        }

        mapper.updateEntity(request, router, site);
        Router updated = repository.save(router);

        logger.info("Router updated successfully with id: {}", updated.getId());

        return mapper.toResponse(updated);
    }


    public RouterResponse update(Long id, RouterPartialUpdateRequest request) {
        logger.info("Partially updating router with id: {}", id);

        Router router = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", id);
                    return new NotFoundException("Router with id " + id + " does not exist");
                });

        NetworkSite site = null;

        if (request.getSiteId() != null) {
            site = networkSiteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> {
                        logger.warn("Network site not found with id: {}", request.getSiteId());
                        return new NotFoundException("Site with id " + request.getSiteId() + " does not exist");
                    });
        }

        if ((request.getHostname() != null && repository.existsByHostnameAndIdNot(request.getHostname(), id)) || (request.getSerialNumber() != null && repository.existsBySerialNumberAndIdNot(request.getSerialNumber(), id))) {
            logger.warn("Cannot partially update router. Hostname or serial number already exists");
            throw new ConflictException("Router with hostname " + request.getHostname() + " or serial number " + request.getSerialNumber() + " already exists");
        }

        mapper.updateEntity(request, router, site);
        Router updated = repository.save(router);

        logger.info("Router partially updated successfully with id: {}", updated.getId());

        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id, boolean cascade) {
        logger.info("Deleting router with id: {} and cascade: {}", id, cascade);

        Router router = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", id);
                    return new NotFoundException("Router with id " + id + " does not exist");
                });

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
                logger.warn("Cannot delete router with id: {} because it has shelves and cascade=false", id);
                throw new ConflictException("Cannot delete router with shelves unless cascade=true");
            }
        }

        repository.delete(router);
        logger.info("Router deleted successfully with id: {}", id);
    }


    public List<ShelfResponse> getShelves(Long id) {
        logger.info("Fetching shelves for router with id: {}", id);
        repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", id);
                    return new NotFoundException("Router with id " + id + " does not exist");
                });
        List<ShelfResponse> shelves = shelfRepository.findByRouterId(id).stream().map(shelfMapper::toResponse).toList();

        logger.info("Found {} shelves for router with id: {}", shelves.size(), id);

        return shelves;
    }

    @Transactional
    public RouterTreeResponse getTree(Long id) {
        logger.info("Fetching router tree for router with id: {}", id);

        Router router = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Router not found with id: {}", id);
                    return new NotFoundException("Router with id " + id + " does not exist");
                });

        RouterTreeResponse response = mapper.toTreeResponse(router);

        logger.info("Router tree fetched successfully for router with id: {}", id);

        return response;
    }


}
