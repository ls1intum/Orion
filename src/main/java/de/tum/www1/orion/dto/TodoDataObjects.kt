package de.tum.www1.orion.dto

/*
 * Data Obects realted to Todos
 */
enum class AttachToType { CLASS, INTERFACE, ENUM, METHOD, FILE }

data class TodoDataObject(
    val file: String,
    val attachedTo: String,
    val attachToType: AttachToType,
    val todo: String
)

data class TodoReference(
    val line: Int,
    val todoText: String
)