CREATE TABLE inventory (
    product_id BIGINT PRIMARY KEY,
    available_quantity INTEGER NOT NULL,
    CONSTRAINT chk_inventory_available_quantity_non_negative CHECK (available_quantity >= 0)
);

CREATE TABLE inventory_reservations (
    reservation_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT fk_inventory_reservation_product FOREIGN KEY (product_id) REFERENCES inventory(product_id),
    CONSTRAINT chk_inventory_reservation_quantity_positive CHECK (quantity > 0)
);
