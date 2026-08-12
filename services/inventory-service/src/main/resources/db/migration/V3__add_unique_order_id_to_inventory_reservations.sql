ALTER TABLE inventory_reservations
ADD CONSTRAINT uq_inventory_reservations_order_id
UNIQUE (order_id);
