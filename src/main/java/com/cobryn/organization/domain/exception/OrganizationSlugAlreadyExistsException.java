package com.cobryn.organization.domain.exception;

public class OrganizationSlugAlreadyExistsException extends RuntimeException {
    public OrganizationSlugAlreadyExistsException(String slug) {
        super(String.format("Organization of the slug '%s' already exists", slug));
    }
    public OrganizationSlugAlreadyExistsException(String slug, Throwable cause) {
        super(String.format("Organization of the slug '%s' already exists", slug), cause);
    }
}
