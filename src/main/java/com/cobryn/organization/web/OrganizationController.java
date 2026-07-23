package com.cobryn.organization.application;

import com.cobryn.organization.OrganizationService;
import com.cobryn.organization.web.dtos.ChangeOrganizationNameRequest;
import com.cobryn.organization.web.dtos.ChangeOrganizationSlugRequest;
import com.cobryn.organization.web.dtos.CreateOrganizationRequest;
import com.cobryn.organization.web.dtos.OrganizationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizationResponse> getOrganizationBySlug(@PathVariable String slug) {
        OrganizationResponse organizationResponse =
               new OrganizationResponse(organizationService.findOrganizationBySlug(slug));

        return ResponseEntity.ok(organizationResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable UUID id) {
        OrganizationResponse organizationResponse =
                new OrganizationResponse(organizationService.findOrganizationById(id));

        return ResponseEntity.ok(organizationResponse);
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestBody CreateOrganizationRequest createOrganizationRequest) {

        OrganizationResponse organization = new OrganizationResponse(
                organizationService.createOrganization(
                        createOrganizationRequest.name(),
                        createOrganizationRequest.slug())
        );

        return new ResponseEntity<>(organization, HttpStatus.CREATED);
    }

    @PutMapping("/slug/{id}")
    public ResponseEntity<OrganizationResponse> changeOrganizationSlug(
            @PathVariable UUID id,
            @RequestBody ChangeOrganizationSlugRequest changeOrganizationSlugRequest) {

        OrganizationResponse organizationWithNewSlug = new OrganizationResponse(
                organizationService.changeOrganizationSlug(id, changeOrganizationSlugRequest.slug())
        );

        return ResponseEntity.ok(organizationWithNewSlug);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> changeOrganizationName(
            @PathVariable UUID id,
            @RequestBody ChangeOrganizationNameRequest changeOrganizationNameRequest) {

        OrganizationResponse organizationWithNewName = new OrganizationResponse(
                organizationService.changeOrganizationName(id, changeOrganizationNameRequest.name())
        );

        return ResponseEntity.ok(organizationWithNewName);
    }
}
