package com.example.inventory.service;

import com.example.inventory.dto.request.UserCreateRequest;
import com.example.inventory.dto.response.UserResponse;
import com.example.inventory.entity.AppUser;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.mapper.AppUserMapper;
import com.example.inventory.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {
    private static final Logger logger = LoggerFactory.getLogger(AppUserService.class);

    private final AppUserMapper mapper;
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;


    public AppUserService(AppUserMapper mapper, AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.mapper = mapper;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserResponse create(UserCreateRequest request) {
        logger.info("Creating account with username: {}", request.getUsername());

        if (repository.existsByUsername(request.getUsername())) {
            logger.warn("Cannot create account. Account with username {} already exists.", request.getUsername());
            throw new ConflictException("Account with username " + request.getUsername() + " already exists.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        AppUser user = mapper.toEntity(request, passwordHash);
        AppUser saved = repository.save(user);
        logger.info("Account created successfully with username: {}", saved.getUsername());
        UserResponse response = mapper.toResponse(saved);
        return response;
    }


}
