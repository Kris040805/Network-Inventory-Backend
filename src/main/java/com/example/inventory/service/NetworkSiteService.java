package com.example.inventory.service;

import com.example.inventory.dto.request.SiteCreateRequest;
import com.example.inventory.dto.request.SiteFullUpdateRequest;
import com.example.inventory.dto.request.SitePartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.RouterResponse;
import com.example.inventory.dto.response.SiteResponse;
import com.example.inventory.entity.NetworkSite;
import com.example.inventory.entity.Router;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.NetworkSiteMapper;
import com.example.inventory.mapper.RouterMapper;
import com.example.inventory.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NetworkSiteService {
    private static final Logger logger = LoggerFactory.getLogger(NetworkSiteService.class);

    private final NetworkSiteMapper mapper;
    private final NetworkSiteRepository repository;
    private final RouterRepository routerRepository;
    private final ShelfRepository shelfRepository;
    private final SlotRepository slotRepository;
    private final CardRepository cardRepository;
    private final RouterMapper routerMapper;


    public NetworkSiteService(NetworkSiteMapper mapper, NetworkSiteRepository repository, RouterRepository routerRepository, ShelfRepository shelfRepository, SlotRepository slotRepository, CardRepository cardRepository, RouterMapper routerMapper) {
        this.mapper = mapper;
        this.repository = repository;
        this.routerRepository = routerRepository;
        this.shelfRepository = shelfRepository;
        this.slotRepository = slotRepository;
        this.cardRepository = cardRepository;
        this.routerMapper = routerMapper;
    }

    // Create site
    public SiteResponse create(SiteCreateRequest request) {
        logger.info("Creating network site with site code: {}", request.getSiteCode());

        if (repository.existsBySiteCode(request.getSiteCode())) {
            logger.warn("Cannot create network site. Site code already exists: {}", request.getSiteCode());
            throw new ConflictException("Site with site code already exists");
        }

        NetworkSite site = mapper.toEntity(request);
        NetworkSite saved = repository.save(site);
        logger.info("Network site created successfully with id: {}", saved.getId());
        SiteResponse response = mapper.toResponse(saved);
        return response;
    }

    // Get site by id
    public SiteResponse getById(Long id) {
        logger.info("Fetching network site with id: {}", id);
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Network site not found with id: {}", id);
                    return new NotFoundException("Site with id " + id + " not found");
                });

        return mapper.toResponse(site);
    }

    // Get all sites in list
    public PageResponse<SiteResponse> getAll(
            Pageable pageable,
            String status,
            String city) {

        logger.info("Fetching network sites with status: {} and city: {}", status, city);
        Page<NetworkSite> sites = repository.findAllByFilters(status, city, pageable);
        logger.info("Found {} network sites", sites.getTotalElements());
        List<SiteResponse> items = sites.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        PageResponse<SiteResponse> response = new PageResponse<>(
                items,
                sites.getNumber(),
                sites.getSize(),
                sites.getTotalElements(),
                sites.getTotalPages()
        );

        return response;
    }

    // Full update
    public SiteResponse update(Long id, SiteFullUpdateRequest request) {
        logger.info("Updating network site with id: {}", id);
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Network site not found with id: {}", id);
                    return new NotFoundException("Site with id " + id + " not found");
                });

        if (repository.existsBySiteCodeAndIdNot(request.getSiteCode(), id)) {
            logger.warn("Cannot update network site. Site code already exists: {}", request.getSiteCode());
            throw new ConflictException("Site with site code " + request.getSiteCode() + " already exists");
        }

        mapper.updateEntity(request, site);
        NetworkSite updated = repository.save(site);
        logger.info("Network site updated successfully with id: {}", updated.getId());

        return mapper.toResponse(updated);
    }

    // Partial update
    public SiteResponse update(Long id, SitePartialUpdateRequest request) {
        logger.info("Partially updating network site with id: {}", id);
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Network site not found with id: {}", id);
                    return new NotFoundException("Site with id " + id + " not found");
                });

        if (request.getSiteCode() != null &&
                repository.existsBySiteCodeAndIdNot(request.getSiteCode(), id)) {
            logger.warn("Cannot partially update network site. Site code already exists: {}", request.getSiteCode());
            throw new ConflictException("Site with site code " + request.getSiteCode() + " already exists");
        }

        mapper.updateEntity(request, site);
        NetworkSite updated = repository.save(site);
        logger.info("Network site partially updated successfully with id: {}", updated.getId());

        return mapper.toResponse(updated);
    }


    @Transactional
    public void delete(Long id, boolean cascade) {
        logger.info("Deleting network site with id: {} and cascade: {}", id, cascade);
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Network site not found with id: {}", id);
                    return new NotFoundException("Site with id " + id + " not found");
                });

        if (repository.existsByIdAndRoutersIsNotEmpty(id)) {
            if (cascade) {
                List<Router> routers = routerRepository.findBySiteId(id);

                for (Router router : routers) {
                    List<Shelf> shelves = shelfRepository.findByRouterId(router.getId());

                    for (Shelf shelf : shelves) {
                        List<Slot> slots = slotRepository.findByShelfId(shelf.getId());

                        for (Slot slot : slots) {
                            cardRepository.deleteBySlotId(slot.getId());
                        }

                        slotRepository.deleteByShelfId(shelf.getId());
                    }

                    shelfRepository.deleteByRouterId(router.getId());
                }

                routerRepository.deleteBySiteId(id);
            } else {
                logger.warn("Cannot delete network site with id: {} because it has routers and cascade=false", id);
                throw new ConflictException("Cannot delete site with routers unless cascade=true");
            }
        }

        repository.delete(site);
        logger.info("Network site deleted successfully with id: {}", id);
    }


    public List<RouterResponse> getRouters(Long id) {
        logger.info("Fetching routers for network site with id: {}", id);
        repository.findById(id).orElseThrow(() -> {
            logger.warn("Network site not found with id: {}", id);
            return new NotFoundException("Site with id " + id + " does not exist");
        });

        List<RouterResponse> routers = routerRepository.findBySiteId(id).stream().map(routerMapper::toResponse).toList();
        logger.info("Found {} routers for network site with id: {}", routers.size(), id);
        return routers;
    }


}
