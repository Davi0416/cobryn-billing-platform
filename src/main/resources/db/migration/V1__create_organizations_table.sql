CREATE TABLE organizations (
                          id UUID PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          slug VARCHAR(80) NOT NULL UNIQUE,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
