package com.cobryn.organization.infrastructure;

import com.cobryn.organization.domain.Organization;
import com.cobryn.organization.domain.OrganizationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrganizationRepositoryAdapter
        implements OrganizationRepository {

    private final JpaOrganizationRepository jpaOrganizationRepository;

    public OrganizationRepositoryAdapter(JpaOrganizationRepository jpaOrganizationRepository) {
        this.jpaOrganizationRepository = jpaOrganizationRepository;
    }

    @Override
    public Organization save(Organization organization) {
        OrganizationEntity savedEntity = jpaOrganizationRepository.save(toEntity(organization));
        return toDomain(savedEntity);
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
        // Usa o construtor de reidratação para preservar o estado persistido do agregado.
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
