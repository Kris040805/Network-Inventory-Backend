package com.example.inventory.service;

import com.example.inventory.dto.request.SiteCreateRequest;
import com.example.inventory.dto.request.SiteFullUpdateRequest;
import com.example.inventory.dto.request.SitePartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.SiteResponse;
import com.example.inventory.entity.NetworkSite;
import com.example.inventory.entity.Router;
import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.NetworkSiteMapper;
import com.example.inventory.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NetworkSiteService {
    private final NetworkSiteMapper mapper;
    private final NetworkSiteRepository repository;
    private final RouterRepository routerRepository;
    private final ShelfRepository shelfRepository;
    private final SlotRepository slotRepository;
    private final CardRepository cardRepository;

    public NetworkSiteService(NetworkSiteMapper mapper, NetworkSiteRepository repository, RouterRepository routerRepository, ShelfRepository shelfRepository, SlotRepository slotRepository, CardRepository cardRepository) {
        this.mapper = mapper;
        this.repository = repository;
        this.routerRepository = routerRepository;
        this.shelfRepository = shelfRepository;
        this.slotRepository = slotRepository;
        this.cardRepository = cardRepository;
    }

    // Create site
    public SiteResponse create(SiteCreateRequest request) {

        if (repository.existsBySiteCode(request.getSiteCode())) {
            throw new ConflictException("Site with site code already exists");
        }

        NetworkSite site = mapper.toEntity(request);
        NetworkSite saved = repository.save(site);
        SiteResponse response = mapper.toResponse(saved);
        return response;
    }

    // Get site by id
    public SiteResponse getById(Long id) {
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Site with id " + id + " not found"));

        return mapper.toResponse(site);
    }

    // Get all sites in list
    public PageResponse<SiteResponse> getAll(
            Pageable pageable,
            String status,
            String city) {

        Page<NetworkSite> sites = repository.findAllByFilters(status, city, pageable);
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
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Site with id " + id + " not found"));

        if (repository.existsBySiteCodeAndIdNot(request.getSiteCode(), id)) {
            throw new ConflictException("Site with site code " + request.getSiteCode() + " already exists");
        }

        mapper.updateEntity(request, site);
        NetworkSite updated = repository.save(site);

        return mapper.toResponse(updated);
    }

    // Partial update
    public SiteResponse update(Long id, SitePartialUpdateRequest request) {
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Site with id " + id + " not found"));

        if (request.getSiteCode() != null &&
                repository.existsBySiteCodeAndIdNot(request.getSiteCode(), id)) {
            throw new ConflictException("Site with site code " + request.getSiteCode() + " already exists");
        }

        mapper.updateEntity(request, site);
        NetworkSite updated = repository.save(site);

        return mapper.toResponse(updated);
    }


    @Transactional
    public void delete(Long id, boolean cascade) {
        NetworkSite site = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Site with id " + id + " not found"));

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
                 throw new ConflictException("Cannot delete site with routers unless cascade=true");
             }
        }

        repository.delete(site);
    }
}
