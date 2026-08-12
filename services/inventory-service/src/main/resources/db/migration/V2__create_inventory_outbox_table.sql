CREATE TABLE inventory_outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
