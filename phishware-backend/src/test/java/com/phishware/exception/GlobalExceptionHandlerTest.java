package com.phishware.exception;

import com.phishware.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        return request;
    }

    @Test
    void unknownUserAndWrongPasswordReturnIdenticalResponse() {
        ResponseEntity<ApiErrorResponse> notFound = handler.handleUserNotFound(
            new UsernameNotFoundException("ghost"), request());
        ResponseEntity<ApiErrorResponse> badCredentials = handler.handleBadCredentials(
            new BadCredentialsException("bad"), request());

        assertEquals(401, notFound.getStatusCode().value());
        assertEquals(401, badCredentials.getStatusCode().value());
        assertEquals(notFound.getBody().getMessage(), badCredentials.getBody().getMessage());
        assertEquals("Credenciales inválidas", notFound.getBody().getMessage());
        assertNull(notFound.getBody().getFieldErrors());
        assertNull(badCredentials.getBody().getFieldErrors());
    }
}
