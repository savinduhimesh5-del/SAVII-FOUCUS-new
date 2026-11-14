package com.savii.focus.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.savii.focus.model.FocusSession
import com.savii.focus.model.FocusTask
import com.savii.focus.model.Subject

/**
 * Small, dependency-free local database. All core data stays on the device.
 */
class FocusDatabase(context: Context) : SQLiteOpenHelper(context, "savii_focus.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE tasks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            due TEXT NOT NULL DEFAULT '',
            done INTEGER NOT NULL DEFAULT 0,
            created INTEGER NOT NULL
        )""")
        db.execSQL("""CREATE TABLE subjects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            detail TEXT NOT NULL DEFAULT '',
            created INTEGER NOT NULL
        )""")
        db.execSQL("""CREATE TABLE sessions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            subject TEXT NOT NULL,
            minutes INTEGER NOT NULL,
            completed INTEGER NOT NULL,
            created INTEGER NOT NULL
        )""")
        db.execSQL("INSERT INTO subjects(name,detail,created) VALUES('Economics','Build your understanding step by step',${System.currentTimeMillis()})")
        db.execSQL("INSERT INTO subjects(name,detail,created) VALUES('Accounting','Practice concepts and questions',${System.currentTimeMillis()})")
        db.execSQL("INSERT INTO subjects(name,detail,created) VALUES('ICT','Learn and revise key topics',${System.currentTimeMillis()})")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun tasks(): List<FocusTask> {
        val out = mutableListOf<FocusTask>()
        readableDatabase.rawQuery("SELECT id,title,due,done FROM tasks ORDER BY done ASC, created DESC", null).use { c ->
            while (c.moveToNext()) out += FocusTask(c.getLong(0), c.getString(1), c.getString(2), c.getInt(3) == 1)
        }
        return out
    }

    fun addTask(title: String, due: String) {
        writableDatabase.execSQL("INSERT INTO tasks(title,due,created) VALUES(?,?,?)", arrayOf(title, due, System.currentTimeMillis()))
    }

    fun toggleTask(id: Long, done: Boolean) {
        writableDatabase.execSQL("UPDATE tasks SET done=? WHERE id=?", arrayOf(if (done) 1 else 0, id))
    }

    fun deleteTask(id: Long) { writableDatabase.delete("tasks", "id=?", arrayOf(id.toString())) }

    fun subjects(): List<Subject> {
        val out = mutableListOf<Subject>()
        readableDatabase.rawQuery("SELECT id,name,detail FROM subjects ORDER BY name", null).use { c ->
            while (c.moveToNext()) out += Subject(c.getLong(0), c.getString(1), c.getString(2))
        }
        return out
    }

    fun addSubject(name: String, detail: String) {
        writableDatabase.execSQL("INSERT INTO subjects(name,detail,created) VALUES(?,?,?)", arrayOf(name, detail, System.currentTimeMillis()))
    }

    fun addSession(subject: String, minutes: Int, completed: Boolean) {
        writableDatabase.execSQL("INSERT INTO sessions(subject,minutes,completed,created) VALUES(?,?,?,?)",
            arrayOf(subject, minutes, if (completed) 1 else 0, System.currentTimeMillis()))
    }

    fun sessions(): List<FocusSession> {
        val out = mutableListOf<FocusSession>()
        readableDatabase.rawQuery("SELECT id,subject,minutes,completed,created FROM sessions ORDER BY created DESC", null).use { c ->
            while (c.moveToNext()) out += FocusSession(c.getLong(0), c.getString(1), c.getInt(2), c.getInt(3) == 1, c.getLong(4))
        }
        return out
    }
}
