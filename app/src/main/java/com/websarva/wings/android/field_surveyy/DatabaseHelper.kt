package com.websarva.wings.android.field_surveyy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.StringBuilder

class DatabaseHelper(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABESE_VERSION){

    companion object{
        private const val DATABASE_NAME="saveImgDB.db"

        private const val DATABESE_VERSION=1
    }

    override fun onCreate(db: SQLiteDatabase){
        val sb=StringBuilder()
        sb.append("CREATE TABLE if not exists saveImgTable(")
        sb.append("name TEXT,")
        sb.append("date TEXT,")
        sb.append("longitude real,")
        sb.append("latitude real,")
        sb.append("azimuth real,")
        sb.append("pitch real,")
        sb.append("roll real")
        sb.append(");")
        val sql=sb.toString()

        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}


}