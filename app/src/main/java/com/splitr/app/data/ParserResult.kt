package com.splitr.app.data

class ItemWithoutIds(
    val name: String,
    var price: Double,
    val quantity: Int,
)

class ParserResult(
    val name: String,
    val date: String,
    val items: List<ItemWithoutIds>
)