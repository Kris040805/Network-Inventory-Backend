package com.example.inventory.mapper;

import com.example.inventory.dto.request.UserCreateRequest;
import com.example.inventory.dto.response.UserResponse;
import com.example.inventory.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AppUserMapper {

    public AppUser toEntity(UserCreateRequest request, String passwordHash) {
        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordHash);
        user.setRole(request.getRole());
        user.setEnabled(true);

        return user;
    }

    public UserResponse toResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }



}
