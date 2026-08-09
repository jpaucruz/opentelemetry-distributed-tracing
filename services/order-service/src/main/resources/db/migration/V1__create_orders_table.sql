CREATE TABLE orders (
    id UUID PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT chk_orders_quantity_positive CHECK (quantity > 0)
);
