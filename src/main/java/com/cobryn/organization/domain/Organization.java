package com.cobryn.organization.domain;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;


@Getter
public class Organization {
    private final UUID id;
    private String name;
    private String slug;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public Organization(String name, String slug) {
        if (name == null || name.isBlank() ) {
            throw new IllegalArgumentException("The name cannot be null or empty.");
        }
        if (slug == null || slug.isBlank() ) {
            throw new IllegalArgumentException("The slug cannot be null or empty.");
        }

        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.slug = slug.trim().toLowerCase();
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Organization(UUID id, String name, String slug, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void activate() {
        if (this.active) {
            return;
        }
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void rename(String name) {
        if (name == null || name.isBlank() ) {
            throw new IllegalArgumentException("The name cannot be null or empty.");
        }
        this.name = name.trim();
        this.updatedAt = Instant.now();
    }

    public void changeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("The slug cannot be null or empty.");
        }

        this.slug = slug.trim().toLowerCase();
        this.updatedAt = Instant.now();
    }
}
