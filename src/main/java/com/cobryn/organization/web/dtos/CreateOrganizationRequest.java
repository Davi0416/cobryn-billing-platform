package com.cobryn.organization.web.dtos;

import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @Size(min = 3, max = 150) String name,
        @Size(min = 2, max = 5) String slug
) {
}
