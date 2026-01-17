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

    @Query("SELECT COUNT(*) FROM product_items WHERE isNotDeleted = 1 AND isScanned = 1")
    int getScannedCount();

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

    @Query("DELETE FROM product_items")
    void deleteAll();
}
