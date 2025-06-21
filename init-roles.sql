-- Initialize base roles
INSERT INTO roles (name, description, created_at, updated_at) VALUES 
('MANAGER', 'Manager role with administrative privileges', NOW(), NOW()),
('EMPLOYEE', 'Standard employee role', NOW(), NOW()),
('ADMIN', 'System administrator role', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Initialize law-specific roles
INSERT INTO law_roles (name, description, base_role_id, created_at, updated_at) VALUES 
('LAWYER', 'Licensed attorney', (SELECT id FROM roles WHERE name = 'MANAGER'), NOW(), NOW()),
('CLERK', 'Legal clerk', (SELECT id FROM roles WHERE name = 'EMPLOYEE'), NOW(), NOW()),
('PARALEGAL', 'Paralegal assistant', (SELECT id FROM roles WHERE name = 'EMPLOYEE'), NOW(), NOW()),
('PARTNER', 'Law firm partner', (SELECT id FROM roles WHERE name = 'MANAGER'), NOW(), NOW()),
('INTERN', 'Legal intern', (SELECT id FROM roles WHERE name = 'EMPLOYEE'), NOW(), NOW()),
('LEGAL_ASSISTANT', 'Legal assistant', (SELECT id FROM roles WHERE name = 'EMPLOYEE'), NOW(), NOW())
ON CONFLICT (name) DO NOTHING; 