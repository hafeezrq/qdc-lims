package com.qdc.lims.repository;

import com.qdc.lims.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    // Find items that are running low
    // SQL: SELECT * FROM items WHERE current_stock <= min_threshold
    List<InventoryItem> findByCurrentStockLessThanEqual(Double threshold);
}