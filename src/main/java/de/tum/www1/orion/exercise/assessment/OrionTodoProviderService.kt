package de.tum.www1.orion.exercise.assessment

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import de.tum.www1.orion.dto.AttachToType
import de.tum.www1.orion.dto.TodoDataObject
import de.tum.www1.orion.util.OrionAssessmentUtils
import de.tum.www1.orion.util.StaticRegex.Companion.JAVA_METHOD_REGEX
import de.tum.www1.orion.util.StaticRegex.Companion.JAVA_TODO_PREFIX
import de.tum.www1.orion.util.StaticRegex.Companion.JAVA_TODO_REGEX
import de.tum.www1.orion.util.StaticRegex.Companion.SLASH_SLASH_COMMENT_REGEX
import kotlinx.collections.immutable.toImmutableList
import java.io.File
import java.io.FileNotFoundException

/**
 * A service that can extract todos from a file
 * At the moment it is limited to JAVA files.
 * It might work with Kotlin too as its structured similar
 *
 * @param project the currently opened project
 */
@Service(Service.Level.PROJECT)
class OrionTodoProviderService(private val project: Project) {


    // A mapping from known Files to Todo-objects
    private var todoMapping: MutableMap<String, MutableList<TodoDataObject>> = mutableMapOf()

    /**
     * @param path the path to the file we take a look at
     * @return A list of todo references. these need to be parsed by some util class to work as comment objects
     */
    private fun initializeTodoForFile(path: String) {

        val todos: MutableList<TodoDataObject> = mutableListOf()
        val lines: MutableList<String>?
        try {
            lines =
                FileUtil.loadLines("${project.basePath}${File.separatorChar}${OrionAssessmentUtils.TEMPLATE}${File.separatorChar}${path}")
        } catch (e: FileNotFoundException) {
            todoMapping[path] = todos
            return
        }
        var structure = ""
        var structureType: AttachToType = AttachToType.FILE
        var currentTodos = ""
        for (line in lines) {
            //filter out comments as comments shouldn't be valid attach tos for code
            // if it matches a task to do regex add a task to do object
            val lineWithoutComments = line.replace(SLASH_SLASH_COMMENT_REGEX, "")

            if (line.matches(JAVA_TODO_REGEX)) {
                currentTodos = currentTodos + line.replaceFirst(JAVA_TODO_PREFIX, "") + "\n"
                continue
            }

            // check if its a new class/interface/enum
            else if (lineWithoutComments.contains(" class ")) {
                if (currentTodos != "") {
                    todos.add(TodoDataObject(path, structure, structureType, currentTodos.removeSuffix("\n")))
                }
                structure = lineWithoutComments.split(" class ")[1]
                structureType = AttachToType.CLASS
            } else if (lineWithoutComments.contains(" interface ")) {
                if (currentTodos != "") {
                    todos.add(TodoDataObject(path, structure, structureType, currentTodos.removeSuffix("\n")))
                }
                structure = lineWithoutComments.split(" interface ")[1]
                structureType = AttachToType.INTERFACE
            } else if (lineWithoutComments.contains(" enum ")) {
                if (currentTodos != "") {
                    todos.add(TodoDataObject(path, structure, structureType, currentTodos.removeSuffix("\n")))
                }
                structure = lineWithoutComments.split(" enum ")[1]
                structureType = AttachToType.ENUM
            } else if (lineWithoutComments.matches(JAVA_METHOD_REGEX)) {
                // figure out name of the structure (its the first string in front of the ( bracket
                if (currentTodos != "") {
                    todos.add(TodoDataObject(path, structure, structureType, currentTodos.removeSuffix("\n")))
                }
                val methodSplit = lineWithoutComments.split("(")[0].trim().split(" ")
                structure = methodSplit[methodSplit.size - 1]
                structureType = AttachToType.METHOD
            }
            // if the line is from no interest continue
            else {
                continue
            }
            currentTodos = ""
        }
        if (currentTodos != "") {
            todos.add(TodoDataObject(path, structure, structureType, currentTodos.removeSuffix("\n")))
        }

        todoMapping[path] = todos
    }

    /**
     * @param path the path to the file we take a look at
     */
    fun getTodoForFile(path: String): List<TodoDataObject> {
        // filter out non java files
        if (!path.endsWith(".java")) {
            return emptyList()
        }
        if (!todoMapping.keys.contains(path)) {
            initializeTodoForFile(path)
        }
        return todoMapping[path]?.toImmutableList() ?: emptyList()
    }
}
