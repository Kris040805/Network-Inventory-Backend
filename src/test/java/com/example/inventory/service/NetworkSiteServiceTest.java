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
public class NetworkSiteServiceTest {

    @Mock
    private NetworkSiteRepository repository;

    @Mock
    private NetworkSiteMapper mapper;

    @Mock
    private RouterRepository routerRepository;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RouterMapper routerMapper;

    @InjectMocks
    private NetworkSiteService service;



    @Test
    void shouldCreateSiteWhenRequestIsValid() {
        SiteCreateRequest request = new SiteCreateRequest();
        request.setSiteCode("TEST01");
        request.setName("Test Site");
        request.setAddress("Test Address");
        request.setCity("Sofia");
        request.setCountryCode("BG");
        request.setStatus("ACTIVE");

        NetworkSite site = new NetworkSite();

        SiteResponse expectedResponse = new SiteResponse();
        expectedResponse.setSiteCode("TEST01");
        expectedResponse.setName("Test Site");
        expectedResponse.setStatus("ACTIVE");

        when(repository.existsBySiteCode("TEST01")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(site);
        when(repository.save(site)).thenReturn(site);
        when(mapper.toResponse(site)).thenReturn(expectedResponse);

        SiteResponse result = service.create(request);

        assertNotNull(result);
        assertEquals("TEST01", result.getSiteCode());
        assertEquals("Test Site", result.getName());
        assertEquals("ACTIVE", result.getStatus());

        verify(repository).existsBySiteCode("TEST01");
        verify(mapper).toEntity(request);
        verify(repository).save(site);
        verify(mapper).toResponse(site);
    }


    @Test
    void shouldThrowConflictExceptionWhenSiteCodeAlreadyExists() {
        SiteCreateRequest request = new SiteCreateRequest();
        request.setSiteCode("TEST01");
        request.setName("Test Site");
        request.setAddress("Test Address");
        request.setCity("Sofia");
        request.setCountryCode("BG");
        request.setStatus("ACTIVE");

        when(repository.existsBySiteCode("TEST01")).thenReturn(true);


        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.create(request)
        );

        assertEquals("Site with site code TEST01 already exists", exception.getMessage());

        verify(repository).existsBySiteCode("TEST01");
        verify(repository, never()).save(any(NetworkSite.class));
        verify(mapper, never()).toEntity(any(SiteCreateRequest.class));
    }

    @Test
    void shouldReturnSiteWhenIdExists() {
        Long siteId = 1L;

        NetworkSite site = new NetworkSite();
        SiteResponse expectedResponse = new SiteResponse();
        expectedResponse.setSiteCode("TEST01");
        expectedResponse.setName("Test Site");
        expectedResponse.setStatus("ACTIVE");

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(mapper.toResponse(site)).thenReturn(expectedResponse);

        SiteResponse result = service.getById(siteId);

        assertNotNull(result);
        assertEquals("TEST01", result.getSiteCode());
        assertEquals("Test Site", result.getName());
        assertEquals("ACTIVE", result.getStatus());

        verify(repository).findById(siteId);
        verify(mapper).toResponse(site);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSiteIdDoesNotExist() {
        Long siteId = 999L;

        when(repository.findById(siteId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.getById(siteId)
        );

        assertEquals("Site with id 999 not found", exception.getMessage());

        verify(repository).findById(siteId);
        verify(mapper, never()).toResponse(any(NetworkSite.class));
    }

    @Test
    void shouldReturnAllSites() {
        Pageable pageable = PageRequest.of(0, 20);

        NetworkSite site1 = new NetworkSite();
        NetworkSite site2 = new NetworkSite();

        SiteResponse response1 = new SiteResponse();
        response1.setSiteCode("TEST01");
        response1.setName("Test Site 1");
        response1.setStatus("ACTIVE");

        SiteResponse response2 = new SiteResponse();
        response2.setSiteCode("TEST02");
        response2.setName("Test Site 2");
        response2.setStatus("PLANNED");

        Page<NetworkSite> sitePage = new PageImpl<>(
                List.of(site1, site2),
                pageable,
                2
        );

        when(repository.findAllByFilters(null, null, pageable)).thenReturn(sitePage);

        when(mapper.toResponse(site1)).thenReturn(response1);
        when(mapper.toResponse(site2)).thenReturn(response2);


        PageResponse<SiteResponse> result = service.getAll(pageable, null, null);

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals("TEST01", result.getItems().get(0).getSiteCode());
        assertEquals("TEST02", result.getItems().get(1).getSiteCode());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(repository).findAllByFilters(null, null, pageable);
        verify(mapper).toResponse(site1);
        verify(mapper).toResponse(site2);
    }

    @Test
    void shouldReturnSitesFilteredByStatusAndCity() {
        Pageable pageable = PageRequest.of(0, 20);
        String status = "ACTIVE";
        String city = "Sofia";

        NetworkSite site = new NetworkSite();

        SiteResponse response = new SiteResponse();
        response.setSiteCode("SOFIA01");
        response.setName("Sofia Site");
        response.setCity("Sofia");
        response.setStatus("ACTIVE");

        Page<NetworkSite> sitePage = new PageImpl<>(
                List.of(site),
                pageable,
                1
        );

        when(repository.findAllByFilters(status, city, pageable))
                .thenReturn(sitePage);

        when(mapper.toResponse(site)).thenReturn(response);

        PageResponse<SiteResponse> result =
                service.getAll(pageable, status, city);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("SOFIA01", result.getItems().get(0).getSiteCode());
        assertEquals("Sofia", result.getItems().get(0).getCity());
        assertEquals("ACTIVE", result.getItems().get(0).getStatus());

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(repository).findAllByFilters(status, city, pageable);
        verify(mapper).toResponse(site);
    }

    @Test
    void shouldUpdateSiteWhenRequestIsValid() {
        Long siteId = 1L;

        SiteFullUpdateRequest request = new SiteFullUpdateRequest();
        request.setSiteCode("UPDATED01");
        request.setName("Updated Site");
        request.setAddress("Updated Address");
        request.setCity("Plovdiv");
        request.setCountryCode("BG");
        request.setStatus("ACTIVE");

        NetworkSite site = new NetworkSite();

        SiteResponse expectedResponse = new SiteResponse();
        expectedResponse.setSiteCode("UPDATED01");
        expectedResponse.setName("Updated Site");
        expectedResponse.setCity("Plovdiv");
        expectedResponse.setStatus("ACTIVE");

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(repository.existsBySiteCodeAndIdNot("UPDATED01", siteId)).thenReturn(false);
        when(repository.save(site)).thenReturn(site);
        when(mapper.toResponse(site)).thenReturn(expectedResponse);

        SiteResponse result = service.update(siteId, request);

        assertNotNull(result);
        assertEquals("UPDATED01", result.getSiteCode());
        assertEquals("Updated Site", result.getName());
        assertEquals("Plovdiv", result.getCity());
        assertEquals("ACTIVE", result.getStatus());

        verify(repository).findById(siteId);
        verify(repository).existsBySiteCodeAndIdNot("UPDATED01", siteId);
        verify(mapper).updateEntity(request, site);
        verify(repository).save(site);
        verify(mapper).toResponse(site);
    }


    @Test
    void shouldThrowConflictExceptionWhenUpdatedSiteCodeAlreadyExists() {
        Long siteId = 1L;

        SiteFullUpdateRequest request = new SiteFullUpdateRequest();
        request.setSiteCode("EXISTING01");
        request.setName("Updated Site");
        request.setAddress("Updated Address");
        request.setCity("Sofia");
        request.setCountryCode("BG");
        request.setStatus("ACTIVE");

        NetworkSite site = new NetworkSite();

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(repository.existsBySiteCodeAndIdNot("EXISTING01", siteId)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.update(siteId, request)
        );

        assertEquals("Site with site code EXISTING01 already exists", exception.getMessage());

        verify(repository).findById(siteId);
        verify(repository).existsBySiteCodeAndIdNot("EXISTING01", siteId);
        verify(mapper, never()).updateEntity(request, site);
        verify(repository, never()).save(any(NetworkSite.class));
    }


    @Test
    void shouldPartiallyUpdateSiteWhenRequestIsValid() {
        Long siteId = 1L;

        SitePartialUpdateRequest request = new SitePartialUpdateRequest();
        request.setName("Updated Site");
        request.setCity("Plovdiv");
        request.setStatus("ACTIVE");

        NetworkSite site = new NetworkSite();

        SiteResponse expectedResponse = new SiteResponse();
        expectedResponse.setSiteCode("TEST01");
        expectedResponse.setName("Updated Site");
        expectedResponse.setCity("Plovdiv");
        expectedResponse.setStatus("ACTIVE");

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(repository.save(site)).thenReturn(site);
        when(mapper.toResponse(site)).thenReturn(expectedResponse);

        SiteResponse result = service.update(siteId, request);

        assertNotNull(result);
        assertEquals("TEST01", result.getSiteCode());
        assertEquals("Updated Site", result.getName());
        assertEquals("Plovdiv", result.getCity());
        assertEquals("ACTIVE", result.getStatus());

        verify(repository).findById(siteId);
        verify(mapper).updateEntity(request, site);
        verify(repository).save(site);
        verify(mapper).toResponse(site);

        verify(repository, never()).existsBySiteCodeAndIdNot(anyString(), anyLong());
    }


    @Test
    void shouldDeleteSiteWhenItHasNoRouters() {
        Long siteId = 1L;

        NetworkSite site = new NetworkSite();

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(repository.existsByIdAndRoutersIsNotEmpty(siteId)).thenReturn(false);

        service.delete(siteId, false);

        verify(repository).findById(siteId);
        verify(repository).existsByIdAndRoutersIsNotEmpty(siteId);
        verify(repository).delete(site);
    }

    @Test
    void shouldThrowConflictExceptionWhenSiteHasRoutersAndCascadeIsFalse() {
        Long siteId = 1L;

        NetworkSite site = new NetworkSite();

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(repository.existsByIdAndRoutersIsNotEmpty(siteId)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.delete(siteId, false)
        );

        assertEquals("Cannot delete site with routers unless cascade=true", exception.getMessage());

        verify(repository).findById(siteId);
        verify(repository).existsByIdAndRoutersIsNotEmpty(siteId);
        verify(repository, never()).delete(any(NetworkSite.class));

        verifyNoInteractions(routerRepository);
        verifyNoInteractions(shelfRepository);
        verifyNoInteractions(slotRepository);
        verifyNoInteractions(cardRepository);
    }

    @Test
    void shouldCascadeDeleteSiteWithRoutersAndChildren() {
        Long siteId = 1L;

        NetworkSite site = new NetworkSite();
        Router router = mock(Router.class);
        Shelf shelf = mock(Shelf.class);
        Slot slot1 = mock(Slot.class);
        Slot slot2 = mock(Slot.class);

        when(router.getId()).thenReturn(10L);
        when(shelf.getId()).thenReturn(20L);
        when(slot1.getId()).thenReturn(30L);
        when(slot2.getId()).thenReturn(31L);

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(repository.existsByIdAndRoutersIsNotEmpty(siteId)).thenReturn(true);
        when(routerRepository.findBySiteId(siteId)).thenReturn(List.of(router));
        when(shelfRepository.findByRouterId(10L)).thenReturn(List.of(shelf));
        when(slotRepository.findByShelfId(20L)).thenReturn(List.of(slot1, slot2));

        service.delete(siteId, true);

        verify(repository).findById(siteId);
        verify(repository).existsByIdAndRoutersIsNotEmpty(siteId);

        verify(routerRepository).findBySiteId(siteId);
        verify(shelfRepository).findByRouterId(10L);
        verify(slotRepository).findByShelfId(20L);

        verify(cardRepository).deleteBySlotId(30L);
        verify(cardRepository).deleteBySlotId(31L);

        verify(slotRepository).deleteByShelfId(20L);
        verify(shelfRepository).deleteByRouterId(10L);
        verify(routerRepository).deleteBySiteId(siteId);

        verify(repository).delete(site);
    }


    @Test
    void shouldReturnRoutersForSite() {
        Long siteId = 1L;

        NetworkSite site = new NetworkSite();

        Router router1 = mock(Router.class);
        Router router2 = mock(Router.class);

        RouterResponse response1 = new RouterResponse();
        response1.setHostname("router-01");

        RouterResponse response2 = new RouterResponse();
        response2.setHostname("router-02");

        when(repository.findById(siteId)).thenReturn(Optional.of(site));
        when(routerRepository.findBySiteId(siteId))
                .thenReturn(List.of(router1, router2));
        when(routerMapper.toResponse(router1)).thenReturn(response1);
        when(routerMapper.toResponse(router2)).thenReturn(response2);

        List<RouterResponse> result = service.getRouters(siteId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("router-01", result.get(0).getHostname());
        assertEquals("router-02", result.get(1).getHostname());

        verify(repository).findById(siteId);
        verify(routerRepository).findBySiteId(siteId);
        verify(routerMapper).toResponse(router1);
        verify(routerMapper).toResponse(router2);
    }


}
