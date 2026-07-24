package com.cobryn.organization.domain.exception;

import java.util.UUID;

public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException(String slug) {
        super(String.format("Organization of the slug: '%s' not found", slug));
    }
    public OrganizationNotFoundException(UUID id) {
        super(String.format("Organization of the id: '%s' not found", id));
    }
}
