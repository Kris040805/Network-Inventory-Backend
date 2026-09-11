package com.example.inventory.controller;

import com.example.inventory.dto.request.RouterCreateRequest;
import com.example.inventory.dto.request.RouterFullUpdateRequest;
import com.example.inventory.dto.request.RouterPartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.RouterResponse;
import com.example.inventory.service.RouterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Pageable;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/routers")
public class RouterController {
    private final RouterService service;


    public RouterController(RouterService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RouterResponse> create(@Valid @RequestBody RouterCreateRequest request) {
        RouterResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<RouterResponse>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String status) {

        PageResponse<RouterResponse> response = service.getAll(pageable, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouterResponse> getById(@PathVariable Long id) {
        RouterResponse response = service.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouterResponse> update(@PathVariable Long id, @Valid @RequestBody RouterFullUpdateRequest request) {
        RouterResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RouterResponse> update(@PathVariable Long id, @Valid @RequestBody RouterPartialUpdateRequest request) {
        RouterResponse response = service.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean cascade) {
        service.delete(id, cascade);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
