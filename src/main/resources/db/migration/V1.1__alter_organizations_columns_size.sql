ALTER TABLE organizations
    ADD CONSTRAINT chk_organizations_name_length
    CHECK (LENGTH(TRIM(name)) >= 3),
    ALTER COLUMN slug TYPE VARCHAR(5),
    ADD CONSTRAINT chk_organizations_slug_length
    CHECK (LENGTH(TRIM(slug)) >= 2);
