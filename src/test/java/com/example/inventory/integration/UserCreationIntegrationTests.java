package com.example.inventory.integration;

import com.example.inventory.entity.AppUser;
import com.example.inventory.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserCreationIntegrationTests {

    private static final String TEST_USERNAME = "it.test.user";
    private static final String TEST_PASSWORD = "Plain#12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository repository;


    @BeforeEach
    void setUp() {
        deleteTestUser();
    }

    @AfterEach
    void tearDown() {
        deleteTestUser();
    }

    private void deleteTestUser() {
        repository.findByUsername(TEST_USERNAME).ifPresent(repository::delete);
    }

    private String userJson(String username, String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
    }


    @Test
    void shouldCreateUserAndAllowLogin() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "Admin#2026"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        AppUser stored = repository.findByUsername(TEST_USERNAME).orElseThrow();
        assertNotEquals(TEST_PASSWORD, stored.getPasswordHash());
        assertTrue(stored.getPasswordHash().startsWith("$2"));

        mockMvc.perform(get("/api/v1/sites")
                        .with(httpBasic(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk());
    }


    @Test
    void shouldCreateUserWithAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "Admin#2026"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "%s",
                                "password": "%s",
                                "role": "ADMIN"
                            }
                            """.formatted(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }


    @Test
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "Admin#2026"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("admin", TEST_PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Account with username admin already exists"));
    }


    @Test
    void shouldReturnBadRequestWhenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "Admin#2026"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(TEST_USERNAME, "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertFalse(repository.existsByUsername(TEST_USERNAME));
    }


    @Test
    void shouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("user", "User#2026"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied: insufficient permissions"));

        assertFalse(repository.existsByUsername(TEST_USERNAME));
    }


    @Test
    void shouldReturnUnauthorizedWhenNoCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        assertFalse(repository.existsByUsername(TEST_USERNAME));
    }

}