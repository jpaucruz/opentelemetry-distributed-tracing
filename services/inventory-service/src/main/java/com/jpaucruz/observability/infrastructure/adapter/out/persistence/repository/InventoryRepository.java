package com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository;

import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    @Modifying
    @Query(
        value = """
            UPDATE inventory
               SET available_quantity = available_quantity - :quantity
             WHERE product_id = :productId
               AND available_quantity >= :quantity
            """,
        nativeQuery = true
    )
    int reserve(@Param("productId") Long productId, @Param("quantity") Integer quantity);

}
