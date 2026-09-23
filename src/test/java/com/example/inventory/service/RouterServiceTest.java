package com.example.inventory.service;

import com.example.inventory.dto.request.RouterCreateRequest;
import com.example.inventory.dto.request.RouterFullUpdateRequest;
import com.example.inventory.dto.request.RouterPartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.RouterResponse;
import com.example.inventory.dto.response.RouterTreeResponse;
import com.example.inventory.dto.response.ShelfResponse;
import com.example.inventory.entity.*;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.mapper.RouterMapper;
import com.example.inventory.mapper.ShelfMapper;
import com.example.inventory.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RouterServiceTest {

    @Mock
    private RouterRepository repository;

    @Mock
    private NetworkSiteRepository siteRepository;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RouterMapper mapper;

    @Mock
    private ShelfMapper shelfMapper;

    @InjectMocks
    private RouterService service;


    @Test
    void shouldCreateRouterWhenRequestIsValid() {
        RouterCreateRequest request = new RouterCreateRequest();
        request.setSiteId(1L);
        request.setHostname("router-01");
        request.setVendor("Cisco");
        request.setModel("ASR1000");
        request.setSerialNumber("SN001");
        request.setManagementIp("192.168.1.10");
        request.setSoftwareVersion("1.0");
        request.setStatus("IN_SERVICE");

        NetworkSite site = new NetworkSite();
        Router router = new Router();

        RouterResponse expectedResponse = new RouterResponse();
        expectedResponse.setHostname("router-01");
        expectedResponse.setSerialNumber("SN001");
        expectedResponse.setStatus("IN_SERVICE");

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(repository.existsByHostnameOrSerialNumber("router-01", "SN001")).thenReturn(false);
        when(mapper.toEntity(request, site)).thenReturn(router);
        when(repository.save(router)).thenReturn(router);
        when(mapper.toResponse(router)).thenReturn(expectedResponse);

        RouterResponse result = service.create(request);

        assertNotNull(result);
        assertEquals("router-01", result.getHostname());
        assertEquals("SN001", result.getSerialNumber());
        assertEquals("IN_SERVICE", result.getStatus());

        verify(siteRepository).findById(1L);
        verify(repository).existsByHostnameOrSerialNumber("router-01", "SN001");
        verify(mapper).toEntity(request, site);
        verify(repository).save(router);
        verify(mapper).toResponse(router);
    }


    @Test
    void shouldThrowConflictExceptionWhenHostnameOrSerialNumberAlreadyExists() {
        RouterCreateRequest request = new RouterCreateRequest();
        request.setSiteId(1L);
        request.setHostname("router-01");
        request.setVendor("Cisco");
        request.setModel("ASR1000");
        request.setSerialNumber("SN001");
        request.setManagementIp("192.168.1.10");
        request.setSoftwareVersion("1.0");
        request.setStatus("IN_SERVICE");

        NetworkSite site = new NetworkSite();

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(repository.existsByHostnameOrSerialNumber("router-01", "SN001")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.create(request)
        );


        assertEquals("Router with hostname or serial number already exists", exception.getMessage());

        verify(siteRepository).findById(1L);
        verify(repository).existsByHostnameOrSerialNumber("router-01", "SN001");
        verify(repository, never()).save(any(Router.class));
        verify(mapper, never()).toEntity(request, site);
    }


    @Test
    void shouldGetAllRouters() {
        Pageable pageable = PageRequest.of(0, 20);

        Router router1 = new Router();
        Router router2 = new Router();

        RouterResponse response1 = new RouterResponse();
        response1.setHostname("router-01");

        RouterResponse response2 = new RouterResponse();
        response2.setHostname("router-02");

        Page<Router> routerPage = new PageImpl<>(
                List.of(router1, router2),
                pageable,
                2
        );

        when(repository.findAllByFilters(null, pageable)).thenReturn(routerPage);
        when(mapper.toResponse(router1)).thenReturn(response1);
        when(mapper.toResponse(router2)).thenReturn(response2);

        PageResponse<RouterResponse> result = service.getAll(pageable, null);

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals("router-01", result.getItems().get(0).getHostname());
        assertEquals("router-02", result.getItems().get(1).getHostname());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(repository).findAllByFilters(null, pageable);
        verify(mapper).toResponse(router1);
        verify(mapper).toResponse(router2);
    }


    @Test
    void shouldGetRouterById() {
        Long routerId = 1L;

        Router router = new Router();

        RouterResponse expectedResponse = new RouterResponse();
        expectedResponse.setHostname("router-01");
        expectedResponse.setSerialNumber("SN001");
        expectedResponse.setStatus("IN_SERVICE");

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(mapper.toResponse(router)).thenReturn(expectedResponse);

        RouterResponse result = service.getById(routerId);

        assertNotNull(result);
        assertEquals("router-01", result.getHostname());
        assertEquals("SN001", result.getSerialNumber());
        assertEquals("IN_SERVICE", result.getStatus());

        verify(repository).findById(routerId);
        verify(mapper).toResponse(router);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouterNotFound() {
        Long routerId = 999L;

        when(repository.findById(routerId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.getById(routerId)
        );

        assertEquals("Router with id 999 does not exist", exception.getMessage());

        verify(repository).findById(routerId);
        verify(mapper, never()).toResponse(any(Router.class));
    }


    @Test
    void shouldUpdateRouter() {
        Long routerId = 1L;

        RouterFullUpdateRequest request = new RouterFullUpdateRequest();
        request.setSiteId(2L);
        request.setHostname("router-02");
        request.setVendor("Cisco");
        request.setModel("ASR9000");
        request.setSerialNumber("SN002");
        request.setManagementIp("192.168.1.20");
        request.setSoftwareVersion("2.0");
        request.setStatus("MAINTENANCE");

        Router router = new Router();
        NetworkSite site = new NetworkSite();

        RouterResponse expectedResponse = new RouterResponse();
        expectedResponse.setHostname("router-02");
        expectedResponse.setSerialNumber("SN002");
        expectedResponse.setStatus("MAINTENANCE");

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(siteRepository.findById(2L)).thenReturn(Optional.of(site));
        when(repository.existsByHostnameAndIdNot("router-02", routerId)).thenReturn(false);
        when(repository.existsBySerialNumberAndIdNot("SN002", routerId)).thenReturn(false);
        when(repository.save(router)).thenReturn(router);
        when(mapper.toResponse(router)).thenReturn(expectedResponse);

        RouterResponse result = service.update(routerId, request);

        assertNotNull(result);
        assertEquals("router-02", result.getHostname());
        assertEquals("SN002", result.getSerialNumber());
        assertEquals("MAINTENANCE", result.getStatus());

        verify(repository).findById(routerId);
        verify(siteRepository).findById(2L);
        verify(repository).existsByHostnameAndIdNot("router-02", routerId);
        verify(repository).existsBySerialNumberAndIdNot("SN002", routerId);
        verify(mapper).updateEntity(request, router, site);
        verify(repository).save(router);
        verify(mapper).toResponse(router);
    }


    @Test
    void shouldThrowConflictExceptionWhenUpdatingRouterWithExistingHostname() {
        Long routerId = 1L;

        RouterFullUpdateRequest request = new RouterFullUpdateRequest();
        request.setSiteId(2L);
        request.setHostname("router-02");
        request.setVendor("Cisco");
        request.setModel("ASR9000");
        request.setSerialNumber("SN002");
        request.setManagementIp("192.168.1.20");
        request.setSoftwareVersion("2.0");
        request.setStatus("MAINTENANCE");

        Router router = new Router();
        NetworkSite site = new NetworkSite();

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(siteRepository.findById(request.getSiteId())).thenReturn(Optional.of(site));
        when(repository.existsByHostnameAndIdNot("router-02", routerId)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.update(routerId, request)
        );

        assertEquals("Router with hostname router-02 or serial number SN002 already exists", exception.getMessage());

        verify(repository).findById(routerId);
        verify(siteRepository).findById(2L);
        verify(repository).existsByHostnameAndIdNot("router-02", routerId);
        verify(mapper, never()).updateEntity(any(RouterFullUpdateRequest.class), any(Router.class), any(NetworkSite.class));
    }


    @Test
    void shouldPartiallyUpdateRouter() {
        Long routerId = 1L;

        RouterPartialUpdateRequest request = new RouterPartialUpdateRequest();
        request.setHostname("router-02");
        request.setSoftwareVersion("2.0");
        request.setStatus("MAINTENANCE");

        Router router = new Router();

        RouterResponse expectedResponse = new RouterResponse();
        expectedResponse.setHostname("router-02");
        expectedResponse.setSoftwareVersion("2.0");
        expectedResponse.setStatus("MAINTENANCE");

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(repository.existsByHostnameAndIdNot("router-02", routerId)).thenReturn(false);
        when(repository.save(router)).thenReturn(router);
        when(mapper.toResponse(router)).thenReturn(expectedResponse);

        RouterResponse result = service.update(routerId, request);

        assertNotNull(result);
        assertEquals("router-02", result.getHostname());
        assertEquals("2.0", result.getSoftwareVersion());
        assertEquals("MAINTENANCE", result.getStatus());

        verify(repository).findById(routerId);
        verify(repository).existsByHostnameAndIdNot("router-02", routerId);
        verify(mapper).updateEntity(request, router, null);
        verify(repository).save(router);
        verify(mapper).toResponse(router);
    }


    @Test
    void shouldDeleteRouterWhenItHasNoShelves() {
        Long routerId = 1L;

        Router router = new Router();

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(repository.existsByIdAndShelvesIsNotEmpty(routerId)).thenReturn(false);

        service.delete(routerId, false);

        verify(repository).findById(routerId);
        verify(repository).existsByIdAndShelvesIsNotEmpty(routerId);
        verify(repository).delete(router);

        verify(shelfRepository, never()).findByRouterId(anyLong());
        verify(slotRepository, never()).findByShelfId(anyLong());
        verify(cardRepository, never()).deleteBySlotId(anyLong());
    }

    @Test
    void shouldThrowConflictExceptionWhenRouterHasShelvesAndCascadeIsFalse() {
        Long routerId = 1L;

        Router router = new Router();

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(repository.existsByIdAndShelvesIsNotEmpty(routerId)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.delete(routerId, false)
        );

        assertEquals("Cannot delete router with shelves unless cascade=true", exception.getMessage());

        verify(repository).findById(routerId);
        verify(repository).existsByIdAndShelvesIsNotEmpty(routerId);
        verify(repository, never()).delete(any(Router.class));

        verify(shelfRepository, never()).findByRouterId(anyLong());
        verify(slotRepository, never()).findByShelfId(anyLong());
        verify(cardRepository, never()).deleteBySlotId(anyLong());
    }


    @Test
    void shouldDeleteRouterWithShelvesWhenCascadeIsTrue() {
        Long routerId = 1L;

        Router router = mock(Router.class);
        Shelf shelf = mock(Shelf.class);
        Slot slot1 = mock(Slot.class);
        Slot slot2 = mock(Slot.class);

        when(shelf.getId()).thenReturn(10L);
        when(slot1.getId()).thenReturn(20L);
        when(slot2.getId()).thenReturn(21L);

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(repository.existsByIdAndShelvesIsNotEmpty(routerId)).thenReturn(true);

        when(shelfRepository.findByRouterId(routerId)).thenReturn(List.of(shelf));
        when(slotRepository.findByShelfId(10L)).thenReturn(List.of(slot1, slot2));

        service.delete(routerId, true);

        verify(repository).findById(routerId);
        verify(repository).existsByIdAndShelvesIsNotEmpty(routerId);

        verify(shelfRepository).findByRouterId(routerId);
        verify(slotRepository).findByShelfId(10L);

        verify(cardRepository).deleteBySlotId(20L);
        verify(cardRepository).deleteBySlotId(21L);

        verify(slotRepository).deleteByShelfId(10L);
        verify(shelfRepository).deleteByRouterId(routerId);

        verify(repository).delete(router);
    }


    @Test
    void shouldGetShelvesForRouter() {
        Long routerId = 1L;

        Router router = new Router();

        Shelf shelf1 = new Shelf();
        Shelf shelf2 = new Shelf();

        ShelfResponse response1 = new ShelfResponse();
        response1.setShelfNumber(1);

        ShelfResponse response2 = new ShelfResponse();
        response2.setShelfNumber(2);

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(shelfRepository.findByRouterId(routerId))
                .thenReturn(List.of(shelf1, shelf2));
        when(shelfMapper.toResponse(shelf1)).thenReturn(response1);
        when(shelfMapper.toResponse(shelf2)).thenReturn(response2);

        List<ShelfResponse> result = service.getShelves(routerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getShelfNumber());
        assertEquals(2, result.get(1).getShelfNumber());

        verify(repository).findById(routerId);
        verify(shelfRepository).findByRouterId(routerId);
        verify(shelfMapper).toResponse(shelf1);
        verify(shelfMapper).toResponse(shelf2);
    }


    @Test
    void shouldGetRouterTree() {
        Long routerId = 1L;

        Router router = new Router();

        RouterTreeResponse expectedResponse = new RouterTreeResponse();

        when(repository.findById(routerId)).thenReturn(Optional.of(router));
        when(mapper.toTreeResponse(router)).thenReturn(expectedResponse);

        RouterTreeResponse result = service.getTree(routerId);

        assertNotNull(result);
        assertSame(expectedResponse, result);

        verify(repository).findById(routerId);
        verify(mapper).toTreeResponse(router);
    }
}
