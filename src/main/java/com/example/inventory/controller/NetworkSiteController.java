package com.example.inventory.controller;

import com.example.inventory.dto.request.SiteCreateRequest;
import com.example.inventory.dto.request.SiteFullUpdateRequest;
import com.example.inventory.dto.request.SitePartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.SiteResponse;
import com.example.inventory.service.NetworkSiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/api/v1/sites")
public class NetworkSiteController {
    private final NetworkSiteService service;

    public NetworkSiteController(NetworkSiteService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<SiteResponse> create(@Valid @RequestBody SiteCreateRequest request) {
        SiteResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> getById(@PathVariable Long id) {
        SiteResponse response = service.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<SiteResponse>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city) {

        PageResponse<SiteResponse> response = service.getAll(pageable, status, city);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteResponse> update(@PathVariable Long id, @Valid @RequestBody SiteFullUpdateRequest request) {
        SiteResponse response = service.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SiteResponse> update(@PathVariable Long id, @Valid @RequestBody SitePartialUpdateRequest request) {
        SiteResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean cascade) {
        service.delete(id, cascade);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
