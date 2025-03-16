package com.splitr.app.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "total_amount") val totalAmount: Double
)

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "receipt_id") val receiptId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price") var price: Double,
    @ColumnInfo(name = "quantity") val quantity: Int
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String
)

@Entity(
    tableName = "user_item_cross_refs",
    primaryKeys = ["user_id", "id"]
)
data class UserItemCrossRef(
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "id") val itemId: Int
)

// Relationship model for Room queries
data class ReceiptWithItems(
    @Embedded val receipt: Receipt,
    @Relation(
        parentColumn = "id",
        entityColumn = "receipt_id"
    )
    val items: List<Item>
)

data class ItemWithUsers(
    @Embedded val item: Item,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(UserItemCrossRef::class)
    )
    val users: List<User>
)

data class ReceiptWithItemsAndUsers(
    @Embedded val receipt: Receipt,
    @Relation(
        entity = Item::class,
        parentColumn = "id",
        entityColumn = "receipt_id"
    )
    val itemsWithUsers: List<ItemWithUsers>
)