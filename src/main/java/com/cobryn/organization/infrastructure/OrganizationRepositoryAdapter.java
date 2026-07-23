package com.cobryn.organization.infrastructure;

import com.cobryn.organization.domain.Organization;
import com.cobryn.organization.domain.OrganizationRepository;
import com.cobryn.organization.domain.exception.OrganizationSlugAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class OrganizationRepositoryAdapter
        implements OrganizationRepository {

    private final JpaOrganizationRepository jpaOrganizationRepository;

    public OrganizationRepositoryAdapter(JpaOrganizationRepository jpaOrganizationRepository) {
        this.jpaOrganizationRepository = jpaOrganizationRepository;
    }

    @Override
    public Organization save(Organization organization) {
        try {
            OrganizationEntity savedEntity = jpaOrganizationRepository.save(toEntity(organization));
            return toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            Throwable root = e.getRootCause() != null ? e.getRootCause() : e;

            if (root instanceof ConstraintViolationException cve) {
                String constraint = cve.getConstraintName();

                if (constraint != null && constraint.toLowerCase().contains("slug")) {
                    log.warn("Slug conflict for '{}': constraint={}", organization.getSlug(), constraint, e);
                    throw new OrganizationSlugAlreadyExistsException(organization.getSlug(), e);
                }
            }

            log.error("Data integrity error saving organization '{}'", organization.getSlug(), e);
            throw e;
        }
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpaOrganizationRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        return jpaOrganizationRepository.findBySlug(slug)
                .map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaOrganizationRepository.existsBySlug(slug);
    }


    private Organization toDomain(OrganizationEntity organizationEntity) {
        return new Organization(
                organizationEntity.getId(),
                organizationEntity.getName(),
                organizationEntity.getSlug(),
                organizationEntity.isActive(),
                organizationEntity.getCreatedAt(),
                organizationEntity.getUpdatedAt());
    }

    private OrganizationEntity toEntity(Organization organization) {
        return new OrganizationEntity(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.isActive(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}
