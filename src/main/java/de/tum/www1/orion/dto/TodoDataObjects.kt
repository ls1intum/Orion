package de.tum.www1.orion.dto

/**
 * An enum specifying different types a TODO can be attached to
 *
 */
enum class AttachToType { CLASS, INTERFACE, ENUM, METHOD, FILE }

/**
 * A Data-Object containing a reference to a specific java construct or to the general file
 * @param file The file the task to do belongs to
 * @param attachedTo the name of the object it is attached to
 * @param attachToType the type of the object
 * @param todo a string containing the content
 */
data class TodoDataObject(
    val file: String,
    val attachedTo: String,
    val attachToType: AttachToType,
    val todo: String
)

/**
 * @param line an integer referencing a line in a file
 * @param todoText the text of a task to do
 */
data class TodoReference(
    val line: Int,
    val todoText: String
)