--liquibase formatted sql

--changeset liquibase:2 splitStatements:false runOnChange:false
-- Insert default roles (Generic roles for infra-core)
INSERT INTO roles (name, description, created_date, updated_date, is_active) 
VALUES 
('ADMIN', 'Administrator role with full access', NOW(), NOW(), TRUE),
('USER', 'Standard user role', NOW(), NOW(), TRUE),
('MANAGER', 'Manager role with supervisory access', NOW(), NOW(), TRUE),
('EMPLOYEE', 'Employee role for staff members', NOW(), NOW(), TRUE),
('CLIENT', 'Client role for external users', NOW(), NOW(), TRUE),
('GUEST', 'Guest role with limited access', NOW(), NOW(), TRUE)
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
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- password: 123123 (yeni hash)
    'admin@lexofis.com',
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

-- Update admin password if it already exists with correct BCrypt hash
UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.' 
WHERE username = 'admin';