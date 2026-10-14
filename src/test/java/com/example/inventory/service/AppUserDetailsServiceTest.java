package com.example.inventory.service;

import com.example.inventory.entity.AppUser;
import com.example.inventory.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppUserDetailsServiceTest {

    @Mock
    private AppUserRepository repository;

    @InjectMocks
    private AppUserDetailsService service;


    @Test
    void shouldReturnCorrectUserWhenExistsAndActive() {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPasswordHash("$2a$10$hashedPasswordExample");
        user.setRole("ADMIN");
        user.setEnabled(true);

        when(repository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("testuser");

        assertEquals("testuser", result.getUsername());
        assertEquals("$2a$10$hashedPasswordExample", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")));

        verify(repository).findByUsername("testuser");
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown")
        );

        assertEquals("User unknown not found", exception.getMessage());

        verify(repository).findByUsername("unknown");
    }


    @Test
    void shouldReturnDisabledUserWhenExists() {
        AppUser user = new AppUser();
        user.setUsername("disableduser");
        user.setPasswordHash("$2a$10$hashedPasswordExample");
        user.setRole("USER");
        user.setEnabled(false);

        when(repository.findByUsername("disableduser")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("disableduser");

        assertEquals("disableduser", result.getUsername());
        assertFalse(result.isEnabled());

        verify(repository).findByUsername("disableduser");
    }


}
