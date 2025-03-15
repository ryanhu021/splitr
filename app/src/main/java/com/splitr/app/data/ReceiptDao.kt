package com.splitr.app.data

import androidx.room.*

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

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAllReceipts(): List<ReceiptWithItems>

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAllReceiptsWithUsers(): List<ReceiptWithItemsAndUsers>

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptById(receiptId: Int): ReceiptWithItems

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptWithUsersById(receiptId: Int): ReceiptWithItemsAndUsers

    @Transaction
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

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
}
