package com.savii.focus.data

import com.savii.focus.model.FocusSession
import com.savii.focus.model.FocusTask
import com.savii.focus.model.Subject

class FocusRepository(private val db: FocusDatabase) {
    fun tasks(): List<FocusTask> = db.tasks()
    fun addTask(title: String, due: String) = db.addTask(title, due)
    fun toggleTask(id: Long, done: Boolean) = db.toggleTask(id, done)
    fun deleteTask(id: Long) = db.deleteTask(id)
    fun subjects(): List<Subject> = db.subjects()
    fun addSubject(name: String, detail: String) = db.addSubject(name, detail)
    fun sessions(): List<FocusSession> = db.sessions()
    fun addSession(subject: String, minutes: Int, completed: Boolean) = db.addSession(subject, minutes, completed)
}
