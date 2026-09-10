package com.example.inventory.controller;

import com.example.inventory.dto.request.CardCreateRequest;
import com.example.inventory.dto.request.CardFullUpdateRequest;
import com.example.inventory.dto.request.CardPartialUpdateRequest;
import com.example.inventory.dto.response.CardResponse;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
    private final CardService service;


    public CardController(CardService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardCreateRequest request) {
        CardResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }


    @GetMapping
    public ResponseEntity<PageResponse<CardResponse>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String status) {

        PageResponse<CardResponse> response = service.getAll(pageable, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getById(@PathVariable Long id) {
        CardResponse response = service.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> update(@PathVariable Long id, @Valid @RequestBody CardFullUpdateRequest request) {
        CardResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CardResponse> update(@PathVariable Long id, @Valid @RequestBody CardPartialUpdateRequest request) {
        CardResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
