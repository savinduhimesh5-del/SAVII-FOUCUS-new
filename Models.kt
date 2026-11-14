package com.savii.focus.model

data class FocusTask(val id: Long, val title: String, val due: String, val done: Boolean)
data class Subject(val id: Long, val name: String, val detail: String)
data class FocusSession(val id: Long, val subject: String, val minutes: Int, val completed: Boolean, val created: Long)
