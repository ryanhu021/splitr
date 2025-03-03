package com.splitr.app.data

import androidx.room.*

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAllReceipts(): List<ReceiptWithItems>

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptById(receiptId: Int): ReceiptWithItems

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Update
    suspend fun updateItem(item: Item)

    @Update
    suspend fun updateReceipt(receipt: Receipt)
}
