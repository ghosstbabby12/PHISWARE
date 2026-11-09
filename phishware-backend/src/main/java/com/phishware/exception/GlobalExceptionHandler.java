package com.phishware.exception;

import com.phishware.dto.response.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(ApiErrorResponse.builder()
            .status(400)
            .error("Validation Error")
            .code("VALIDATION_FAILED")
            .message("Los datos enviados contienen errores de validación")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .fieldErrors(fieldErrors)
            .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(
            404, "Not Found", "RESOURCE_NOT_FOUND", ex.getMessage(), request.getRequestURI()
        ));
    }

    @ExceptionHandler(UrlValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleUrlValidation(
            UrlValidationException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(buildError(
            400, "Invalid URL", "INVALID_URL", ex.getMessage(), request.getRequestURI()
        ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UsernameNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(
            401, "Unauthorized", "AUTH_INVALID_CREDENTIALS", "Credenciales inválidas", request.getRequestURI()
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(
            401, "Unauthorized", "AUTH_INVALID_CREDENTIALS", "Credenciales inválidas", request.getRequestURI()
        ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildError(
            403, "Forbidden", "ACCOUNT_DISABLED", "Cuenta desactivada", request.getRequestURI()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildError(
            403, "Forbidden", "FORBIDDEN", "No tienes permisos para realizar esta acción", request.getRequestURI()
        ));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiErrorResponse> handleSecurityException(
            SecurityException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(buildError(
            400, "Security Violation", "SECURITY_VIOLATION", ex.getMessage(), request.getRequestURI()
        ));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(
            401, "Unauthorized", "INVALID_TOKEN", "Token inválido o sesión expirada", request.getRequestURI()
        ));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(buildError(
            400, "Bad Request", "MALFORMED_REQUEST", "La solicitud tiene un formato inválido", request.getRequestURI()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(buildError(
            400, "Bad Request", "BAD_REQUEST", ex.getMessage(), request.getRequestURI()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericError(
            Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError(
            500, "Internal Server Error", "INTERNAL_ERROR",
            "Ha ocurrido un error interno. Por favor intente más tarde.",
            request.getRequestURI()
        ));
    }

    private ApiErrorResponse buildFieldError(int status, String error, String field,
                                             String message, String path) {
        ApiErrorResponse response = buildError(status, error, "VALIDATION_FAILED", message, path);
        response.setFieldErrors(Map.of(field, message));
        return response;
    }

    private ApiErrorResponse buildError(int status, String error, String code, String message, String path) {
        return ApiErrorResponse.builder()
            .status(status)
            .error(error)
            .code(code)
            .message(message)
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
