--liquibase formatted sql

--changeset fatih:10 labels:v1.1
--comment: Insert test clients
INSERT INTO clients (username, email, first_name, last_name, enabled, active, phone_number, address, notes, created_date, last_modified_date, version, deleted) VALUES
('ahmet.yilmaz', 'ahmet.yilmaz@email.com', 'Ahmet', 'Yılmaz', true, true, '+90 555 123 4567', 'Beşiktaş, İstanbul', 'İş hukuku uzmanı avukat', NOW(), NOW(), 0, false),
('zeynep.kaya', 'zeynep.kaya@email.com', 'Zeynep', 'Kaya', true, true, '+90 555 987 6543', 'Çankaya, Ankara', 'Aile hukuku davalarında müvekkil', NOW(), NOW(), 0, false),
('mehmet.celik', 'mehmet.celik@email.com', 'Mehmet', 'Çelik', true, true, '+90 555 555 1234', 'Konak, İzmir', 'Ticaret hukuku müvekkili', NOW(), NOW(), 0, false);

--changeset fatih:11 labels:v1.1
--comment: Insert test cases for clients
INSERT INTO cases (case_number, title, description, status, type, filing_date, client_id, created_date, last_modified_date, version, deleted) VALUES
('CASE-2024-0001', 'İş Sözleşmesi Feshi Davası', 'Haksız yere iş sözleşmesi feshi nedeniyle açılan dava', 'IN_PROGRESS', 'CIVIL', '2024-01-15', 1, NOW(), NOW(), 0, false),
('CASE-2024-0002', 'Boşanma Davası', 'Anlaşmalı boşanma davası', 'OPEN', 'FAMILY', '2024-02-10', 2, NOW(), NOW(), 0, false),
('CASE-2024-0003', 'Ticaret Uyuşmazlığı', 'Sözleşme ihlali davası', 'PENDING', 'CORPORATE', '2024-03-05', 3, NOW(), NOW(), 0, false),
('CASE-2024-0004', 'Araç Değer Kaybı', 'Trafik kazası sonrası araç değer kaybı davası', 'OPEN', 'CAR_DEPRECIATION', '2024-03-20', 1, NOW(), NOW(), 0, false); 