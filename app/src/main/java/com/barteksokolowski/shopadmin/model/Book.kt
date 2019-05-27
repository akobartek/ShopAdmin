package com.barteksokolowski.shopadmin.model

import android.database.Cursor

data class Book(var _ID: Long, var title: String, var authors: String, var category: Int,
                var photoURL: String, var price: Double, var note: String) {

    companion object {
        fun fromCursor(cursor: Cursor): Book {
            return Book(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getDouble(5),
                    cursor.getString(6)
            )
        }
    }
}