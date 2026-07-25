package com.cobryn.organization.web.dtos;

import jakarta.validation.constraints.Size;

public record ChangeOrganizationSlugRequest(
        @Size(min = 2, max = 5) String slug
) {
}
