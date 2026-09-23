package com.example.inventory.controller;


import com.example.inventory.dto.request.SiteCreateRequest;
import com.example.inventory.dto.request.SiteFullUpdateRequest;
import com.example.inventory.dto.request.SitePartialUpdateRequest;
import com.example.inventory.dto.response.PageResponse;
import com.example.inventory.dto.response.RouterResponse;
import com.example.inventory.dto.response.SiteResponse;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.service.NetworkSiteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NetworkSiteController.class)
public class NetworkSiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NetworkSiteService service;


    @Test
    void shouldGetSiteById() throws Exception {
        Long siteId = 1L;

        SiteResponse response = new SiteResponse();
        response.setId(siteId);
        response.setSiteCode("SITE-001");
        response.setName("Sofia Site");
        response.setCity("Sofia");
        response.setCountryCode("BG");
        response.setStatus("ACTIVE");

        when(service.getById(siteId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/sites/{id}", siteId).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.siteCode").value("SITE-001")).andExpect(jsonPath("$.name").value("Sofia Site")).andExpect(jsonPath("$.city").value("Sofia")).andExpect(jsonPath("$.countryCode").value("BG")).andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service).getById(siteId);
    }


    @Test
    void shouldGetAllSites() throws Exception {
        SiteResponse site = new SiteResponse();
        site.setId(1L);
        site.setSiteCode("SITE-001");
        site.setName("Sofia Site");
        site.setCity("Sofia");
        site.setCountryCode("BG");
        site.setStatus("ACTIVE");

        PageResponse<SiteResponse> response = new PageResponse<>(List.of(site), 0, 20, 1, 1);

        Pageable pageable = PageRequest.of(0, 20);

        when(service.getAll(any(Pageable.class), eq("ACTIVE"), eq("Sofia"))).thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/sites")
                                .param("page", "0")
                                .param("size", "20")
                                .param("status", "ACTIVE")
                                .param("city", "Sofia")
                                .accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].siteCode").value("SITE-001"))
                .andExpect(jsonPath("$.items[0].name").value("Sofia Site"))
                .andExpect(jsonPath("$.items[0].city").value("Sofia"))
                .andExpect(jsonPath("$.items[0].countryCode").value("BG"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(service).getAll(any(Pageable.class), eq("ACTIVE"), eq("Sofia"));
    }

    @Test
    void shouldCreateSite() throws Exception {
        SiteResponse response = new SiteResponse();
        response.setId(1L);
        response.setSiteCode("SITE-001");
        response.setName("Sofia Site");
        response.setCity("Sofia");
        response.setCountryCode("BG");
        response.setStatus("ACTIVE");

        when(service.create(any(SiteCreateRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/sites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                        {
                                                            "siteCode": "SITE-001",
                                                            "name": "Sofia Site",
                                                            "address": "Sofia",
                                                            "city": "Sofia",
                                                            "countryCode": "BG",
                                                            "latitude": 42.6977,
                                                            "longitude": 23.3219,
                                                            "status": "ACTIVE"
                                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/sites/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.siteCode").value("SITE-001"))
                .andExpect(jsonPath("$.name").value("Sofia Site"))
                .andExpect(jsonPath("$.city").value("Sofia"))
                .andExpect(jsonPath("$.countryCode").value("BG"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service).create(any(SiteCreateRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenSiteDoesNotExist() throws Exception {
        Long siteId = 999L;

        when(service.getById(siteId)).thenThrow(new NotFoundException("Site with id " + siteId + " not found"));

        mockMvc.perform(get("/api/v1/sites/{id}", siteId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Site with id 999 not found"));

        verify(service).getById(siteId);
    }


    @Test
    void shouldReturnBadRequestWhenCreatingSiteWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/v1/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "siteCode": "SITE-001",
                                "name": "",
                                "address": "Sofia",
                                "city": "Sofia",
                                "countryCode": "BG",
                                "latitude": 42.6977,
                                "longitude": 23.3219,
                                "status": "ACTIVE"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }


    @Test
    void shouldUpdateSite() throws Exception {
        Long siteId = 1L;

        SiteResponse response = new SiteResponse();
        response.setId(siteId);
        response.setSiteCode("SITE-001");
        response.setName("Updated Sofia Site");
        response.setCity("Sofia");
        response.setCountryCode("BG");
        response.setStatus("ACTIVE");

        when(service.update(eq(siteId), any(SiteFullUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/sites/{id}", siteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "siteCode": "SITE-001",
                                "name": "Updated Sofia Site",
                                "address": "Updated address",
                                "city": "Sofia",
                                "countryCode": "BG",
                                "latitude": 42.6977,
                                "longitude": 23.3219,
                                "status": "ACTIVE"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.siteCode").value("SITE-001"))
                .andExpect(jsonPath("$.name").value("Updated Sofia Site"))
                .andExpect(jsonPath("$.city").value("Sofia"))
                .andExpect(jsonPath("$.countryCode").value("BG"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service).update(eq(siteId), any(SiteFullUpdateRequest.class));
    }



    @Test
    void shouldPartiallyUpdateSite() throws Exception {
        Long siteId = 1L;

        SiteResponse response = new SiteResponse();
        response.setId(siteId);
        response.setSiteCode("SITE-001");
        response.setName("Updated Sofia Site");
        response.setCity("Sofia");
        response.setCountryCode("BG");
        response.setStatus("ACTIVE");

        when(service.update(eq(siteId), any(SitePartialUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/sites/{id}", siteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Updated Sofia Site"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.siteCode").value("SITE-001"))
                .andExpect(jsonPath("$.name").value("Updated Sofia Site"))
                .andExpect(jsonPath("$.city").value("Sofia"))
                .andExpect(jsonPath("$.countryCode").value("BG"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service).update(eq(siteId), any(SitePartialUpdateRequest.class));
    }


    @Test
    void shouldDeleteSite() throws Exception {
        Long siteId = 1L;

        doNothing().when(service).delete(siteId, false);

        mockMvc.perform(delete("/api/v1/sites/{id}", siteId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).delete(siteId, false);
    }


    @Test
    void shouldGetRoutersForSite() throws Exception {
        Long siteId = 1L;

        RouterResponse router = new RouterResponse();
        router.setId(10L);
        router.setSiteId(siteId);
        router.setHostname("router-01");
        router.setVendor("Cisco");
        router.setModel("ASR1000");
        router.setSerialNumber("RTR-001");
        router.setStatus("IN_SERVICE");

        when(service.getRouters(siteId))
                .thenReturn(List.of(router));

        mockMvc.perform(get("/api/v1/sites/{id}/routers", siteId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].siteId").value(1))
                .andExpect(jsonPath("$[0].hostname").value("router-01"))
                .andExpect(jsonPath("$[0].vendor").value("Cisco"))
                .andExpect(jsonPath("$[0].model").value("ASR1000"))
                .andExpect(jsonPath("$[0].serialNumber").value("RTR-001"))
                .andExpect(jsonPath("$[0].status").value("IN_SERVICE"));

        verify(service).getRouters(siteId);
    }


    @Test
    void shouldReturnConflictWhenCreatingSiteWithDuplicateSiteCode() throws Exception {
        when(service.create(any(SiteCreateRequest.class)))
                .thenThrow(new ConflictException("Site with site code SITE-001 already exists"));

        mockMvc.perform(post("/api/v1/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "siteCode": "SITE-001",
                                "name": "Sofia Site",
                                "address": "Sofia",
                                "city": "Sofia",
                                "countryCode": "BG",
                                "latitude": 42.6977,
                                "longitude": 23.3219,
                                "status": "ACTIVE"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Site with site code SITE-001 already exists"));

        verify(service).create(any(SiteCreateRequest.class));
    }

}
