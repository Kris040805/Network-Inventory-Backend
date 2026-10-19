package com.example.inventory.service;

import com.example.inventory.dto.request.UserCreateRequest;
import com.example.inventory.dto.response.UserResponse;
import com.example.inventory.entity.AppUser;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.mapper.AppUserMapper;
import com.example.inventory.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppUserServiceTest {

    @Mock
    private AppUserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private AppUserMapper mapper = new AppUserMapper();

    @InjectMocks
    private AppUserService service;


    @Test
    void shouldCreateEnabledUserWithHashedPassword() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("Plain#12345");

        when(repository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Plain#12345")).thenReturn("$2a$10$hashedPassword");
        when(repository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.create(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());
        AppUser saved = captor.getValue();

        assertEquals("newuser", saved.getUsername());
        assertEquals("$2a$10$hashedPassword", saved.getPasswordHash());
        assertNotEquals("Plain#12345", saved.getPasswordHash());
        assertEquals("USER", saved.getRole());
        assertTrue(saved.isEnabled());

        assertEquals("newuser", response.getUsername());
        assertEquals("USER", response.getRole());
        assertTrue(response.isEnabled());
    }


    @Test
    void shouldCreateUserWithRequestedRole() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newadmin");
        request.setPassword("Plain#12345");
        request.setRole("ADMIN");

        when(repository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("Plain#12345")).thenReturn("$2a$10$hashedPassword");
        when(repository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.create(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());

        assertEquals("ADMIN", captor.getValue().getRole());
        assertEquals("ADMIN", response.getRole());
    }


    @Test
    void shouldThrowConflictWhenUsernameAlreadyExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existing");
        request.setPassword("Plain#12345");

        when(repository.existsByUsername("existing")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.create(request)
        );

        assertEquals("Account with username existing already exists", exception.getMessage());

        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).save(any());
    }

}