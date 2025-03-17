package com.splitr.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserItemCrossRef(userItemCrossRef: UserItemCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserReceiptCrossRef(userReceiptCrossRef: UserReceiptCrossRef)

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAllReceipts(): List<ReceiptWithItems>

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAllReceiptsWithUsers(): List<ReceiptWithItemsAndUsers>

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptById(receiptId: Int): Receipt

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptWithItemsById(receiptId: Int): ReceiptWithItems

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptWithItemsAndUsersById(receiptId: Int): ReceiptWithItemsAndUsers

    @Transaction
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptWithUsersById(receiptId: Int): ReceiptWithUsers

    @Delete
    suspend fun deleteUserReceiptCrossRef(userReceiptCrossRef: UserReceiptCrossRef)

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Delete
    suspend fun deleteItem(item: Item)

    @Delete
    suspend fun deleteUser(user: User)

    @Delete
    suspend fun deleteUserItemCrossRef(userItemCrossRef: UserItemCrossRef)

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Update
    suspend fun updateItem(item: Item)

    @Update
    suspend fun updateUser(user: User)

    // Total calculation

    @Transaction
    @Query("SELECT SUM(price * quantity) FROM items WHERE receipt_id = :receiptId")
    suspend fun calculateTotalForReceipt(receiptId: Int): Double

    @Transaction
    suspend fun updateReceiptTotal(receiptId: Int, total: Double) {
        // Update the total amount of the receipt
        val receipt = getReceiptById(receiptId).copy(totalAmount = total)
        updateReceipt(receipt)
    }

    @Transaction
    suspend fun insertItemsAndUpdateTotal(items: List<Item>) {
        insertItems(items)
        // After inserting items, update the receipt total
        items.forEach { item ->
            val total = calculateTotalForReceipt(item.receiptId)
            updateReceiptTotal(item.receiptId, total)
        }
    }

    @Transaction
    suspend fun updateItemAndUpdateTotal(item: Item)  {
        updateItem(item)
        // After updating an item, recalculate the total for the receipt
        val total = calculateTotalForReceipt(item.receiptId)
        updateReceiptTotal(item.receiptId, total)
    }
}
