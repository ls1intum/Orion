package de.tum.www1.orion.ui.assessment

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.jetbrains.rd.generator.nova.immutableList
import kotlinx.collections.immutable.toImmutableList

@Service(Service.Level.PROJECT)
class TodoProvider(private val project: Project) {

    enum class AttachToType { CLASS, METHOD, FILE }
    data class TodoDataObject(
        val file: String,
        val attachedTo: String,
        val attachToType: AttachToType,
        val todo: String
    )

    // A mapping from known Files to Todo-objects
    private var todoMapping: MutableMap<String, MutableList<TodoDataObject>> = mutableMapOf()

    private val JAVA_METHOD_REGEX =
        Regex("\\b(?:public|private|protected|static|final|\\s)*\\s+\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*.*")
    private val JAVA_TODO_REGEX = Regex(".*(\\sTODO.?\\s).*")
    private val JAVA_TODO_PREFIX = Regex(".*(\\sTODO.?\\s)")

    /**
     * @param path the path to the file we take a look at
     * @return A list of todo references. these need to be parsed by some util class to work as comment objects
     */
    fun initializeTodoForFile(path: String) {
        val todos: MutableList<TodoDataObject> = mutableListOf()

        val lines = FileUtil.loadLines(path)
        var currentStructure = ""
        var currentStructureType: AttachToType = AttachToType.FILE
        lines.forEachIndexed { index, element ->
            // check if its a new class/interface/enum
            if (element.contains("class") || element.contains("interface") || element.contains("enum")) {
                // figure out name
                currentStructure = ""
                currentStructureType = AttachToType.CLASS
            }

            if (element.matches(JAVA_METHOD_REGEX)) {
                currentStructure = ""
                currentStructureType = AttachToType.METHOD
            }
            // if it matches todo regex add a todo object
            if (element.matches(JAVA_TODO_REGEX)) {
                todos.add(
                    TodoDataObject(
                        path,
                        currentStructure,
                        currentStructureType,
                        element.replaceFirst(JAVA_TODO_PREFIX, "")
                    )
                )
            }
        }

        todoMapping[path] = todos
    }

    /**
     * @param path the path to the file we take a look at
     */
    fun getTodoForFile(path: String): List<TodoDataObject> {
        if (!todoMapping.keys.contains(path)) {
            initializeTodoForFile(path)
        }
        return todoMapping.get(path)?.toImmutableList() ?: emptyList()
    }

}