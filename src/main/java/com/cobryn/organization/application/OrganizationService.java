package com.cobryn.organization.application;

import com.cobryn.organization.domain.Organization;
import com.cobryn.organization.domain.OrganizationRepository;
import com.cobryn.organization.domain.exception.OrganizationNotFoundException;
import com.cobryn.organization.domain.exception.OrganizationSlugAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository
    ) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public Organization createOrganization(String name, String slug) {
        String normalizedSlug = normalizeSlug(slug);

        checkIfSlugAlreadyExists(normalizedSlug);

        Organization organization = new Organization(name, normalizedSlug);

        try {
            return organizationRepository.save(organization);
        } catch (DataIntegrityViolationException e) {
            throw new OrganizationSlugAlreadyExistsException(normalizedSlug, e);
        }
    }

    public Organization findOrganizationBySlug(String slug) {
        String normalizedSlug = normalizeSlug(slug);

        return organizationRepository.findBySlug(normalizedSlug)
                .orElseThrow(() ->
                        new OrganizationNotFoundException(normalizedSlug));
    }

    public Organization findOrganizationById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new OrganizationNotFoundException(id));
    }

    @Transactional
    public Organization changeOrganizationName(UUID id, String name) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new OrganizationNotFoundException(id));

        organization.rename(name);

        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization changeOrganizationSlug(UUID id, String slug) {
        String normalizedSlug = normalizeSlug(slug);

        checkIfSlugAlreadyExists(normalizedSlug);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new OrganizationNotFoundException(id));

        organization.changeSlug(normalizedSlug);

        try {
            return organizationRepository.save(organization);
        } catch (DataIntegrityViolationException e) {
            throw new OrganizationSlugAlreadyExistsException(normalizedSlug, e);
        }
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    private void checkIfSlugAlreadyExists(String slug) {
        if (organizationRepository.existsBySlug(slug)) {
            throw new OrganizationSlugAlreadyExistsException(slug);
        }
    }
}
