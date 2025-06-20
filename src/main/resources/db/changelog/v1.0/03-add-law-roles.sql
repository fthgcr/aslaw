--liquibase formatted sql

--changeset fatih:add-law-roles splitStatements:false runOnChange:false
--comment: Add law-specific role system

-- Create law_roles table
CREATE TABLE IF NOT EXISTS law_roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    base_role_id BIGINT NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (base_role_id) REFERENCES roles(id)
);

-- Create law_users table (composition with users)
CREATE TABLE IF NOT EXISTS law_users (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    bar_number VARCHAR(50) UNIQUE,
    specialization VARCHAR(255),
    experience_years INTEGER,
    law_school VARCHAR(255),
    graduation_year INTEGER,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create law_user_roles junction table
CREATE TABLE IF NOT EXISTS law_user_roles (
    law_user_id BIGINT NOT NULL,
    law_role_id BIGINT NOT NULL,
    PRIMARY KEY (law_user_id, law_role_id),
    FOREIGN KEY (law_user_id) REFERENCES law_users(id) ON DELETE CASCADE,
    FOREIGN KEY (law_role_id) REFERENCES law_roles(id) ON DELETE CASCADE
);

-- Insert law-specific roles
INSERT INTO law_roles (name, description, base_role_id, created_date, updated_date, is_active)
SELECT 
    'LAWYER', 
    'Lawyer with legal practice authority', 
    r.id, 
    NOW(), 
    NOW(), 
    TRUE
FROM roles r WHERE r.name = 'MANAGER'
ON CONFLICT (name) DO NOTHING;

INSERT INTO law_roles (name, description, base_role_id, created_date, updated_date, is_active)
SELECT 
    'CLERK', 
    'Legal clerk providing administrative support', 
    r.id, 
    NOW(), 
    NOW(), 
    TRUE
FROM roles r WHERE r.name = 'EMPLOYEE'
ON CONFLICT (name) DO NOTHING;

INSERT INTO law_roles (name, description, base_role_id, created_date, updated_date, is_active)
SELECT 
    'PARALEGAL', 
    'Paralegal providing legal assistance', 
    r.id, 
    NOW(), 
    NOW(), 
    TRUE
FROM roles r WHERE r.name = 'EMPLOYEE'
ON CONFLICT (name) DO NOTHING;

INSERT INTO law_roles (name, description, base_role_id, created_date, updated_date, is_active)
SELECT 
    'PARTNER', 
    'Law firm partner with ownership stakes', 
    r.id, 
    NOW(), 
    NOW(), 
    TRUE
FROM roles r WHERE r.name = 'MANAGER'
ON CONFLICT (name) DO NOTHING;

INSERT INTO law_roles (name, description, base_role_id, created_date, updated_date, is_active)
SELECT 
    'INTERN', 
    'Law student intern', 
    r.id, 
    NOW(), 
    NOW(), 
    TRUE
FROM roles r WHERE r.name = 'EMPLOYEE'
ON CONFLICT (name) DO NOTHING;

INSERT INTO law_roles (name, description, base_role_id, created_date, updated_date, is_active)
SELECT 
    'LEGAL_ASSISTANT', 
    'Legal assistant providing administrative support', 
    r.id, 
    NOW(), 
    NOW(), 
    TRUE
FROM roles r WHERE r.name = 'EMPLOYEE'
ON CONFLICT (name) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_law_roles_base_role ON law_roles(base_role_id);
CREATE INDEX IF NOT EXISTS idx_law_users_user_id ON law_users(user_id);
CREATE INDEX IF NOT EXISTS idx_law_users_bar_number ON law_users(bar_number);
CREATE INDEX IF NOT EXISTS idx_law_user_roles_law_user_id ON law_user_roles(law_user_id);
CREATE INDEX IF NOT EXISTS idx_law_user_roles_law_role_id ON law_user_roles(law_role_id); 