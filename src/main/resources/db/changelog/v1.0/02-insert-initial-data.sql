--liquibase formatted sql

--changeset liquibase:2 splitStatements:false runOnChange:false
-- Insert default roles
INSERT INTO roles (name, description, created_date, updated_date, is_active) 
VALUES 
('ADMIN', 'Administrator role with full access', NOW(), NOW(), TRUE),
('USER', 'Standard user role', NOW(), NOW(), TRUE),
('LAWYER', 'Lawyer role with legal access', NOW(), NOW(), TRUE)
ON CONFLICT (name) DO NOTHING;

-- Insert default admin user
INSERT INTO users (
    username, 
    password, 
    email, 
    first_name, 
    last_name, 
    enabled, 
    is_active,
    deleted,
    created_date, 
    last_modified_date,
    updated_date
) 
VALUES (
    'admin',
    '$2a$10$9VfJl8gHIe7C4R6V4KxVmu7lq9qx1gJP1Vp7vfvC4oJm8qO4H2K9G', -- password: admin123
    'admin@lawportal.com',
    'System',
    'Admin',
    TRUE,
    TRUE,
    FALSE,
    NOW(),
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;