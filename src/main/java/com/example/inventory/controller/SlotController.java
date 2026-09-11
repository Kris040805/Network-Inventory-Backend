package com.example.inventory.controller;

import com.example.inventory.dto.request.SlotCreateRequest;
import com.example.inventory.dto.request.SlotFullUpdateRequest;
import com.example.inventory.dto.request.SlotPartialUpdateRequest;
import com.example.inventory.dto.response.CardResponse;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.SlotResponse;
import com.example.inventory.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/slots")
public class SlotController {
    private final SlotService service;


    public SlotController(SlotService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SlotResponse> create(@Valid @RequestBody SlotCreateRequest request) {
        SlotResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<SlotResponse>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String status
    ) {
        PageResponse<SlotResponse> response = service.getAll(pageable, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SlotResponse> getById(@PathVariable Long id) {
        SlotResponse response = service.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SlotResponse> update(@PathVariable Long id, @Valid @RequestBody SlotFullUpdateRequest request) {
        SlotResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SlotResponse> update(@PathVariable Long id, @Valid @RequestBody SlotPartialUpdateRequest request) {
        SlotResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean cascade) {
        service.delete(id, cascade);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/{id}/card")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long id) {
        CardResponse response = service.getCard(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}











