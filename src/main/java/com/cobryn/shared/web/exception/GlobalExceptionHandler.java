package com.cobryn.shared.web.exception;

import com.cobryn.organization.domain.exception.OrganizationNotFoundException;
import com.cobryn.organization.domain.exception.OrganizationSlugAlreadyExistsException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrganizationNotFound(
            OrganizationNotFoundException ex,
            HttpServletRequest request
    ) {

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(OrganizationSlugAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleOrganizationSlugAlreadyExistsException(
            OrganizationSlugAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
