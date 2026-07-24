package com.cobryn.organization.web.dtos;

public record CreateOrganizationRequest(
        String name,
        String slug
) {
}
