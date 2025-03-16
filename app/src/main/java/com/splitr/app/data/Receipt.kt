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
    primaryKeys = ["user_id", "id"],
    // TODO: Double check this -> Used for automatic clean up in case the item or user is deleted
//    foreignKeys = [
//        ForeignKey(
//            entity = User::class,
//            parentColumns = ["id"],
//            childColumns = ["user_id"],
//            onDelete = ForeignKey.CASCADE
//        ),
//        ForeignKey(
//            entity = Item::class,
//            parentColumns = ["receipt_id"],
//            childColumns = ["id"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ]
)
data class UserItemCrossRef(
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "id") val itemId: Int
)

@Entity(
    tableName = "user_receipt_cross_refs",
    primaryKeys = ["user_id", "id"],
    // TODO: Double check this -> Used for automatic clean up in case the item or user is deleted
//    foreignKeys = [
//        ForeignKey(
//            entity = User::class,
//            parentColumns = ["id"],
//            childColumns = ["user_id"],
//            onDelete = ForeignKey.CASCADE
//        ),
//        ForeignKey(
//            entity = Receipt::class,
//            parentColumns = ["id"],
//            childColumns = ["receipt_id"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ]
)
data class UserReceiptCrossRef(
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "id") val receiptId: Int
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
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = UserItemCrossRef::class,
            parentColumn = "id",
            entityColumn = "user_id"
        )
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

data class ReceiptWithUsers(
    @Embedded val receipt: Receipt,
    @Relation(
        parentColumn = "id",
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = UserReceiptCrossRef::class,
            parentColumn = "id",
            entityColumn = "user_id"
        )
    )
    val users: List<User>
)