package com.example.dora.database.dbmodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
class Contacts {

    @PrimaryKey(autoGenerate = true)
    private val uid: Int = 0

    @ColumnInfo(name = "name")
    private var name: String = ""

    @ColumnInfo(name = "phoneNumber")
    private var phone_number:String = ""

    @ColumnInfo(name = "email")
    private var email:String = ""

}