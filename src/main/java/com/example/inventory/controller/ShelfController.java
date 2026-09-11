package com.example.inventory.controller;


import com.example.inventory.dto.request.ShelfCreateRequest;
import com.example.inventory.dto.request.ShelfFullUpdateRequest;
import com.example.inventory.dto.request.ShelfPartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.ShelfResponse;
import com.example.inventory.dto.response.SlotResponse;
import com.example.inventory.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shelves")
public class ShelfController {
    private final ShelfService service;


    public ShelfController(ShelfService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ShelfResponse> create(@Valid @RequestBody ShelfCreateRequest request) {
        ShelfResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ShelfResponse>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String status) {
        PageResponse<ShelfResponse> response = service.getAll(pageable, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShelfResponse> getById(@PathVariable Long id) {
        ShelfResponse response = service.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShelfResponse> update(@PathVariable Long id, @Valid @RequestBody ShelfFullUpdateRequest request) {
        ShelfResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShelfResponse> update(@PathVariable Long id, @Valid @RequestBody ShelfPartialUpdateRequest request) {
        ShelfResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean cascade) {
        service.delete(id, cascade);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotResponse>> getSlots(@PathVariable Long id) {
        List<SlotResponse> response = service.getSlots(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
