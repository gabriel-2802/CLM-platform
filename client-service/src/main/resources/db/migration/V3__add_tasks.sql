-- ============================================================================
-- table: clients.tasks
-- ============================================================================
CREATE TABLE clients.tasks (
                               id        BIGSERIAL PRIMARY KEY,
                               done      BOOLEAN      NOT NULL DEFAULT FALSE,
                               title     VARCHAR(500) NOT NULL,
                               notes     TEXT,
                               blocked   TEXT,
                               objective TEXT,
                               date      TIMESTAMP    NOT NULL,
                               user_id   BIGINT       NOT NULL,
                               client_id BIGINT       NOT NULL,
                               CONSTRAINT fk_tasks_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_client_id ON clients.tasks(client_id);
CREATE INDEX idx_tasks_user_id   ON clients.tasks(user_id);
CREATE INDEX idx_tasks_date      ON clients.tasks(date);