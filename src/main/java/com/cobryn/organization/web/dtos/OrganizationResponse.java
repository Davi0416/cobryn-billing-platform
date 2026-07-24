package com.cobryn.organization.web.dtos;

import com.cobryn.organization.domain.Organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        boolean active,
        Instant createdAt,
        Instant updatedAt
){
    public OrganizationResponse(Organization organization) {
        this(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.isActive(),
                organization.getCreatedAt(),
                organization.getUpdatedAt());
    }
}
