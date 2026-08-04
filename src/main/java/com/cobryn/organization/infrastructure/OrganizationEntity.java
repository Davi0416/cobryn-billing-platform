package com.cobryn.organization.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    private UUID id;
    private String name;
    private String slug;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;


    public OrganizationEntity(UUID id, String name, String slug, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor exigido pelo JPA ao materializar entidades do banco de dados.
    public OrganizationEntity() {

    }
}
