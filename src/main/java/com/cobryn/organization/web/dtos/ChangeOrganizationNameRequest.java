package com.cobryn.organization.web.dtos;

import jakarta.validation.constraints.Size;

public record ChangeOrganizationNameRequest (
        @Size(min = 3, max = 150) String name
){
}
