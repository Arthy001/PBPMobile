package com.example.uhf_bt.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.uhf_bt.models.ProductItemEntity;
import java.util.List;

@Dao
public interface ProductItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ProductItemEntity> items);

    @Update
    void update(ProductItemEntity item);

    @Query("SELECT * FROM product_items WHERE isNotDeleted = 1")
    List<ProductItemEntity> getAll();

    @Query("SELECT * FROM product_items WHERE tagNumber = :tag LIMIT 1")
    ProductItemEntity findByTag(String tag);

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND productItemStatus = 1")
    int getStockTargetCount();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1")
    int getCountAll();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1")
    int getTotalItemCount();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND isScanned = 1")
    int getScannedCount();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND isScanned = 1")
    int getScannedItemCount();

    @Query("SELECT * FROM product_items WHERE isNotDeleted = 1 AND isScanned = 1")
    List<ProductItemEntity> getScannedItems();

    @Query("SELECT * FROM product_items WHERE isNotDeleted = 1 AND productItemStatus = :status")
    List<ProductItemEntity> getItemsByStatus(int status);

    @Query("SELECT * FROM product_items WHERE isNotDeleted = 1 AND productItemStatus NOT IN (1, 2, 3, 7)")
    List<ProductItemEntity> getOtherStatusItems();

    @Query("SELECT * FROM product_items WHERE isNotDeleted = 1 AND isScanned = 0")
    List<ProductItemEntity> getRemainingItems();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND productItemStatus = :status")
    int getCountByStatus(int status);

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND productItemStatus = :status AND isScanned = 1")
    int getScannedCountByStatus(int status);

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND productItemStatus NOT IN (1, 2, 3, 7)")
    int getOtherStatusCount();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND productItemStatus NOT IN (1, 2, 3, 7) AND isScanned = 1")
    int getScannedOtherStatusCount();

    @Query("DELETE FROM product_items WHERE isNotDeleted = 0")
    void deleteMarkedItems();

    @Query("UPDATE product_items SET isScanned = 0")
    void clearScannedStatus();

    @Query("UPDATE product_items SET isScannedStockCount = 0")
    void clearScannedStockStatus();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND isScannedStockCount = 1")
    int getScannedStockCount();

    @Query("SELECT * FROM product_items WHERE isNotDeleted = 1 AND isScannedStockCount = 1")
    List<ProductItemEntity> getScannedStockItems();

    @Query("SELECT * FROM product_items WHERE isNotDeleted = 1 AND isScannedStockCount = 0")
    List<ProductItemEntity> getRemainingStockItems();

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND productItemStatus = :status AND isScannedStockCount = 1")
    int getScannedStockCountByStatus(int status);

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND productItemStatus NOT IN (1, 2, 3, 7) AND isScannedStockCount = 1")
    int getScannedStockOtherStatusCount();

    @Query("SELECT * FROM product_items LIMIT 1")
    ProductItemEntity getAnyItem();

    // Filter query methods for StockCountFragment
    @Query("SELECT * FROM product_items WHERE productName IN (:productNames) AND isNotDeleted = 1")
    List<ProductItemEntity> getItemsByProductNames(List<String> productNames);

    @Query("SELECT * FROM product_items WHERE productGroupName IN (:groupNames) AND isNotDeleted = 1")
    List<ProductItemEntity> getItemsByProductGroups(List<String> groupNames);

    @Query("SELECT * FROM product_items WHERE warehouseName IN (:warehouseNames) AND isNotDeleted = 1")
    List<ProductItemEntity> getItemsByWarehouses(List<String> warehouseNames);

    // Combined filter query - supports multiple criteria
    // Note: Room doesn't support null checks with IN clause, so we check list sizes instead
    @Query("SELECT * FROM product_items WHERE " +
           "(CASE WHEN :productNamesSize = 0 THEN 1 ELSE productName IN (:productNames) END) = 1 AND " +
           "(CASE WHEN :groupNamesSize = 0 THEN 1 ELSE productGroupName IN (:groupNames) END) = 1 AND " +
           "(CASE WHEN :warehouseNamesSize = 0 THEN 1 ELSE warehouseName IN (:warehouseNames) END) = 1 AND " +
           "(:status IS NULL OR productItemStatus = :status) AND " +
           "isNotDeleted = 1")
    List<ProductItemEntity> getFilteredItems(
        List<String> productNames,
        int productNamesSize,
        List<String> groupNames,
        int groupNamesSize,
        List<String> warehouseNames,
        int warehouseNamesSize,
        Integer status
    );

    // Get distinct values for filter dropdowns
    @Query("SELECT DISTINCT productName FROM product_items WHERE isNotDeleted = 1 AND productName IS NOT NULL ORDER BY productName")
    List<String> getAllProductNames();

    @Query("SELECT DISTINCT productGroupName FROM product_items WHERE isNotDeleted = 1 AND productGroupName IS NOT NULL ORDER BY productGroupName")
    List<String> getAllProductGroupNames();

    @Query("SELECT DISTINCT warehouseName FROM product_items WHERE isNotDeleted = 1 AND warehouseName IS NOT NULL ORDER BY warehouseName")
    List<String> getAllWarehouseNames();

    @Query("DELETE FROM product_items")
    void deleteAll();
}
