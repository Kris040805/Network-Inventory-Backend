package com.example.inventory.config;

import com.example.inventory.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication failed: invalid or missing credentials",
                request.getRequestURI()
        );


        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Network Inventory\"");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), error);
    }

}
