package com.example.inventory.controller;

import com.example.inventory.config.RestAccessDeniedHandler;
import com.example.inventory.config.RestAuthenticationEntryPoint;
import com.example.inventory.config.SecurityConfig;
import com.example.inventory.dto.request.UserCreateRequest;
import com.example.inventory.dto.response.UserResponse;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@WithMockUser(roles = "ADMIN")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppUserService service;


    @Test
    void shouldCreateUser() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("newuser");
        response.setRole("USER");
        response.setEnabled(true);

        when(service.create(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "newuser",
                                "password": "Plain#12345"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(service).create(any(UserCreateRequest.class));
    }


    @Test
    void shouldReturnBadRequestWhenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "newuser",
                                "password": "short"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }


    @Test
    void shouldReturnBadRequestWhenRoleIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "newuser",
                                "password": "Plain#12345",
                                "role": "SUPERUSER"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }


    @Test
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        when(service.create(any(UserCreateRequest.class)))
                .thenThrow(new ConflictException("User with username newuser already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "newuser",
                                "password": "Plain#12345"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User with username newuser already exists"));

        verify(service).create(any(UserCreateRequest.class));
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "newuser",
                                "password": "Plain#12345"
                            }
                            """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied: insufficient permissions"));

        verifyNoInteractions(service);
    }


    @Test
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "newuser",
                                "password": "Plain#12345"
                            }
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(service);
    }

}