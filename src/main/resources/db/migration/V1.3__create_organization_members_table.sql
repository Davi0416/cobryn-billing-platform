CREATE TABLE organization_members
(
    organization_id UUID                     NOT NULL,
    user_id         UUID                     NOT NULL,
    role            VARCHAR(30)              NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_organization_members
        PRIMARY KEY (organization_id, user_id),

    CONSTRAINT fk_organization_members_organization
        FOREIGN KEY (organization_id)
            REFERENCES organizations (id),

    CONSTRAINT fk_organization_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT chk_organization_members_role
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER'))
);
