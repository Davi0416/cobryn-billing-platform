package com.cobryn.shared.web.exception;

import com.cobryn.organization.domain.exception.OrganizationNotFoundException;
import com.cobryn.organization.domain.exception.OrganizationSlugAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.transaction.TransactionSystemException;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Locale;

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

    @ExceptionHandler({DataIntegrityViolationException.class, TransactionSystemException.class})
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        if (!isSlugConflict(ex)) {
            throw ex;
        }

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Organization of the slug already exists.",
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private boolean isSlugConflict(Throwable ex) {
        Throwable current = ex;

        // Exceções de persistência podem encapsular o erro original em vários níveis.
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException) {
                String constraintName = constraintViolationException.getConstraintName();
                if (constraintName != null && constraintName.toLowerCase(Locale.ROOT).contains("slug")) {
                    return true;
                }

                SQLException sqlException = constraintViolationException.getSQLException();
                if (sqlException != null && "23505".equals(sqlException.getSQLState())) {
                    return true;
                }
            }

            if (current instanceof SQLException sqlException && "23505".equals(sqlException.getSQLState())) {
                return true;
            }

            String message = current.getMessage();
            if (message != null) {
                String normalizedMessage = message.toLowerCase(Locale.ROOT);
                if (normalizedMessage.contains("duplicate key value violates unique constraint")
                        && normalizedMessage.contains("slug")) {
                    return true;
                }
            }

            current = current.getCause();
        }
        return false;
    }
}
