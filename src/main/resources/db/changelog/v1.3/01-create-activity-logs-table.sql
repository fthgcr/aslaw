--liquibase formatted sql

--changeset aslaw:create-activity-logs-table-v1.3.1
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    performed_by_id BIGINT NOT NULL,
    performed_by_name VARCHAR(255) NOT NULL,
    performed_by_username VARCHAR(100) NOT NULL,
    target_entity_id BIGINT NOT NULL,
    target_entity_name VARCHAR(255) NOT NULL,
    target_entity_type VARCHAR(50) NOT NULL,
    related_entity_id BIGINT,
    related_entity_name VARCHAR(255),
    related_entity_type VARCHAR(50),
    details TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

--rollback DROP TABLE activity_logs;

--changeset aslaw:create-activity-logs-indexes-v1.3.2
CREATE INDEX idx_activity_logs_created_date ON activity_logs(created_date DESC);
CREATE INDEX idx_activity_logs_performed_by_id ON activity_logs(performed_by_id);
CREATE INDEX idx_activity_logs_target_entity ON activity_logs(target_entity_id, target_entity_type);
CREATE INDEX idx_activity_logs_type ON activity_logs(type);

--rollback DROP INDEX idx_activity_logs_created_date;
--rollback DROP INDEX idx_activity_logs_performed_by_id;
--rollback DROP INDEX idx_activity_logs_target_entity;
--rollback DROP INDEX idx_activity_logs_type; 