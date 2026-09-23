package com.example.inventory.integration;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;


@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldCompleteSiteCrudFlow() throws Exception {

        // POST
        MvcResult result = mockMvc.perform(
                        post("/api/v1/sites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                        {
                                                            "siteCode": "TEST-SITE-001",
                                                            "name": "Integration Test Site",
                                                            "address": "Test Address",
                                                            "city": "Sofia",
                                                            "countryCode": "BG",
                                                            "latitude": 42.6977,
                                                            "longitude": 23.3219,
                                                            "status": "ACTIVE"
                                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.siteCode").value("TEST-SITE-001"))
                .andExpect(jsonPath("$.name").value("Integration Test Site"))
                .andExpect(jsonPath("$.city").value("Sofia"))
                .andExpect(jsonPath("$.countryCode").value("BG"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        JsonNode json = new ObjectMapper().readTree(response);

        Long siteId = json.get("id").asLong();


        // GET
        mockMvc.perform(get("/api/v1/sites/{id}", siteId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(siteId))
                .andExpect(jsonPath("$.siteCode").value("TEST-SITE-001"))
                .andExpect(jsonPath("$.name").value("Integration Test Site"))
                .andExpect(jsonPath("$.city").value("Sofia"))
                .andExpect(jsonPath("$.countryCode").value("BG"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));


        // PUT
        mockMvc.perform(put("/api/v1/sites/{id}", siteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                           {
                                                                               "siteCode": "TEST-SITE-001",
                                                                               "name": "Updated Integration Test Site",
                                                                               "address": "Updated Test Address",
                                                                               "city": "Plovdiv",
                                                                               "countryCode": "BG",
                                                                               "latitude": 42.1354,
                                                                               "longitude": 24.7453,
                                                                               "status": "ACTIVE"
                                                                           }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(siteId))
                .andExpect(jsonPath("$.siteCode").value("TEST-SITE-001"))
                .andExpect(jsonPath("$.name").value("Updated Integration Test Site"))
                .andExpect(jsonPath("$.address").value("Updated Test Address"))
                .andExpect(jsonPath("$.city").value("Plovdiv"))
                .andExpect(jsonPath("$.countryCode").value("BG"))
                .andExpect(jsonPath("$.latitude").value(42.1354))
                .andExpect(jsonPath("$.longitude").value(24.7453))
                .andExpect(jsonPath("$.status").value("ACTIVE"));


        // PATCH
        mockMvc.perform(
                        patch("/api/v1/sites/{id}", siteId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                      {
                                                        "name": "Patched Integration Test Site"
                                                      }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(siteId))
                .andExpect(jsonPath("$.siteCode").value("TEST-SITE-001"))
                .andExpect(jsonPath("$.name").value("Patched Integration Test Site"))
                .andExpect(jsonPath("$.address").value("Updated Test Address"))
                .andExpect(jsonPath("$.city").value("Plovdiv"))
                .andExpect(jsonPath("$.countryCode").value("BG"))
                .andExpect(jsonPath("$.latitude").value(42.1354))
                .andExpect(jsonPath("$.longitude").value(24.7453))
                .andExpect(jsonPath("$.status").value("ACTIVE"));


        // DELETE
        mockMvc.perform(
                        delete("/api/v1/sites/{id}", siteId))
                .andExpect(status().isNoContent());


        // GET (should be Not Found)
        mockMvc.perform(
                        get("/api/v1/sites/{id}", siteId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));


    }


    @Test
    void shouldCompleteNetworkInventoryLifecycle() throws Exception {

        // SITE
        MvcResult siteResult = mockMvc.perform(
                        post("/api/v1/sites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "siteCode": "TEST-LIFECYCLE-001",
                                            "name": "Lifecycle Test Site",
                                            "address": "Test Address",
                                            "city": "Sofia",
                                            "countryCode": "BG",
                                            "latitude": 42.6977,
                                            "longitude": 23.3219,
                                            "status": "ACTIVE"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.siteCode").value("TEST-LIFECYCLE-001"))
                .andExpect(jsonPath("$.name").value("Lifecycle Test Site"))
                .andReturn();

        JsonNode siteJson = new ObjectMapper().readTree(siteResult.getResponse().getContentAsString());

        Long siteId = siteJson.get("id").asLong();


        // ROUTER
        MvcResult routerResult = mockMvc.perform(
                        post("/api/v1/routers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "siteId": %d,
                                            "hostname": "lifecycle-router-01",
                                            "vendor": "Cisco",
                                            "model": "ASR1000",
                                            "serialNumber": "LIFECYCLE-RTR-001",
                                            "managementIp": "192.168.1.10",
                                            "softwareVersion": "17.1",
                                            "status": "IN_SERVICE"
                                        }
                                        """.formatted(siteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.siteId").value(siteId))
                .andExpect(jsonPath("$.hostname").value("lifecycle-router-01"))
                .andExpect(jsonPath("$.serialNumber").value("LIFECYCLE-RTR-001"))
                .andExpect(jsonPath("$.status").value("IN_SERVICE"))
                .andReturn();

        JsonNode routerJson = new ObjectMapper()
                .readTree(routerResult.getResponse().getContentAsString());

        Long routerId = routerJson.get("id").asLong();


        // SHELF
        MvcResult shelfResult = mockMvc.perform(
                        post("/api/v1/shelves")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "routerId": %d,
                                            "shelfNumber": 1,
                                            "shelfType": "STANDARD",
                                            "serialNumber": "LIFECYCLE-SHELF-001",
                                            "totalSlots": 4,
                                            "status": "IN_SERVICE"
                                        }
                                        """.formatted(routerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.routerId").value(routerId))
                .andExpect(jsonPath("$.shelfNumber").value(1))
                .andExpect(jsonPath("$.serialNumber").value("LIFECYCLE-SHELF-001"))
                .andExpect(jsonPath("$.totalSlots").value(4))
                .andExpect(jsonPath("$.status").value("IN_SERVICE"))
                .andReturn();

        JsonNode shelfJson = new ObjectMapper()
                .readTree(shelfResult.getResponse().getContentAsString());

        Long shelfId = shelfJson.get("id").asLong();


        // SLOT
        MvcResult slotResult = mockMvc.perform(
                        post("/api/v1/slots")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "shelfId": %d,
                                            "slotNumber": 1,
                                            "slotType": "LINE",
                                            "status": "EMPTY"
                                        }
                                        """.formatted(shelfId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.shelfId").value(shelfId))
                .andExpect(jsonPath("$.slotNumber").value(1))
                .andExpect(jsonPath("$.slotType").value("LINE"))
                .andExpect(jsonPath("$.status").value("EMPTY"))
                .andReturn();

        JsonNode slotJson = new ObjectMapper()
                .readTree(slotResult.getResponse().getContentAsString());

        Long slotId = slotJson.get("id").asLong();


        // INSTALL CARD
        MvcResult cardResult = mockMvc.perform(
                        post("/api/v1/slots/{id}/card", slotId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "partNumber": "PN-LIFECYCLE-001",
                                            "serialNumber": "LIFECYCLE-CARD-001",
                                            "cardType": "LINE_CARD",
                                            "portCount": 24,
                                            "hardwareRevision": "REV-A"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.partNumber").value("PN-LIFECYCLE-001"))
                .andExpect(jsonPath("$.serialNumber").value("LIFECYCLE-CARD-001"))
                .andExpect(jsonPath("$.cardType").value("LINE_CARD"))
                .andExpect(jsonPath("$.portCount").value(24))
                .andExpect(jsonPath("$.hardwareRevision").value("REV-A"))
                .andExpect(jsonPath("$.status").value("INSTALLED"))
                .andReturn();

        JsonNode cardJson = new ObjectMapper()
                .readTree(cardResult.getResponse().getContentAsString());

        Long cardId = cardJson.get("id").asLong();


        // TREE
        mockMvc.perform(
                        get("/api/v1/routers/{id}/tree", routerId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routerId))
                .andExpect(jsonPath("$.hostname").value("lifecycle-router-01"))
                .andExpect(jsonPath("$.shelves[0].id").value(shelfId))
                .andExpect(jsonPath("$.shelves[0].shelfNumber").value(1))
                .andExpect(jsonPath("$.shelves[0].slots[0].id").value(slotId))
                .andExpect(jsonPath("$.shelves[0].slots[0].slotNumber").value(1))
                .andExpect(jsonPath("$.shelves[0].slots[0].card.id").value(cardId))
                .andExpect(jsonPath("$.shelves[0].slots[0].card.serialNumber")
                        .value("LIFECYCLE-CARD-001"))
                .andExpect(jsonPath("$.shelves[0].slots[0].card.status")
                        .value("INSTALLED"));


        // REMOVE CARD
        mockMvc.perform(
                        delete("/api/v1/slots/{id}/card", slotId))
                .andExpect(status().isNoContent());

        // VERIFY CARD REMOVED
        mockMvc.perform(
                        get("/api/v1/slots/{id}/card", slotId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // VERIFY SLOT IS EMPTY
        mockMvc.perform(
                        get("/api/v1/slots/{id}", slotId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(slotId))
                .andExpect(jsonPath("$.status").value("EMPTY"));


        // DELETE SITE
        mockMvc.perform(
                        delete("/api/v1/sites/{id}", siteId)
                                .param("cascade", "true"))
                .andExpect(status().isNoContent());


        // VERIFY SITE DELETED
        mockMvc.perform(
                        get("/api/v1/sites/{id}", siteId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }


}
