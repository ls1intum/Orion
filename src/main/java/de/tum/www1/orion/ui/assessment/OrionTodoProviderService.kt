package de.tum.www1.orion.ui.assessment

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import de.tum.www1.orion.dto.AttachToType
import de.tum.www1.orion.dto.TodoDataObject
import de.tum.www1.orion.ui.util.StaticRegex.Companion.JAVA_METHOD_REGEX
import de.tum.www1.orion.ui.util.StaticRegex.Companion.JAVA_TODO_PREFIX
import de.tum.www1.orion.ui.util.StaticRegex.Companion.JAVA_TODO_REGEX
import de.tum.www1.orion.ui.util.StaticRegex.Companion.SLASH_SLASH_COMMENT_REGEX
import de.tum.www1.orion.util.OrionAssessmentUtils
import kotlinx.collections.immutable.toImmutableList
import java.io.File

@Service(Service.Level.PROJECT)
class OrionTodoProviderService(private val project: Project) {


    // A mapping from known Files to Todo-objects
    private var todoMapping: MutableMap<String, MutableList<TodoDataObject>> = mutableMapOf()

    /**
     * @param path the path to the file we take a look at
     * @return A list of todo references. these need to be parsed by some util class to work as comment objects
     */
    fun initializeTodoForFile(path: String) {

        val todos: MutableList<TodoDataObject> = mutableListOf()
        val lines =
            FileUtil.loadLines("${project.basePath}${File.separatorChar}${OrionAssessmentUtils.TEMPLATE}${File.separatorChar}${path}")
        var currentStructure = ""
        var currentStructureType: AttachToType = AttachToType.FILE
        var currentFeedback = ""
        for (element in lines) {
            //filter out comments as comments shouldn't be valid attach tos for code
            // if it matches todo regex add a todo object
            val elementWithoutComments = element.replace(SLASH_SLASH_COMMENT_REGEX, "")

            if (element.matches(JAVA_TODO_REGEX)) {
                currentFeedback = currentFeedback + element.replaceFirst(JAVA_TODO_PREFIX, "") + "\n"
                continue
            }

            // check if its a new class/interface/enum
            else if (elementWithoutComments.contains(" class ")) {
                if (currentFeedback != "") {
                    todos.add(
                        TodoDataObject(
                            path,
                            currentStructure,
                            currentStructureType,
                            currentFeedback.removeSuffix("\n")
                        )
                    )
                }
                currentStructure = elementWithoutComments.split(" class ")[1]
                currentStructureType = AttachToType.CLASS
            } else if (elementWithoutComments.contains(" interface ")) {
                if (currentFeedback != "") {
                    todos.add(
                        TodoDataObject(
                            path,
                            currentStructure,
                            currentStructureType,
                            currentFeedback.removeSuffix("\n")
                        )
                    )
                }
                currentStructure = elementWithoutComments.split(" interface ")[1]
                currentStructureType = AttachToType.INTERFACE
            } else if (elementWithoutComments.contains(" enum ")) {
                if (currentFeedback != "") {
                    todos.add(
                        TodoDataObject(
                            path,
                            currentStructure,
                            currentStructureType,
                            currentFeedback.removeSuffix("\n")
                        )
                    )
                }
                currentStructure = elementWithoutComments.split(" enum ")[1]
                currentStructureType = AttachToType.ENUM
            } else if (elementWithoutComments.matches(JAVA_METHOD_REGEX)) {
                // figure out name of the structure (its the first string in front of the ( bracket
                if (currentFeedback != "") {
                    todos.add(
                        TodoDataObject(
                            path,
                            currentStructure,
                            currentStructureType,
                            currentFeedback.removeSuffix("\n")
                        )
                    )
                }
                val structureList = elementWithoutComments.split("(")
                val methodSplit = structureList[0].trim().split(" ")
                currentStructure = methodSplit[methodSplit.size - 1]
                currentStructureType = AttachToType.METHOD
            }
            // if the line is from no interest continue
            else {
                continue
            }
            currentFeedback = ""
        }
        if (currentFeedback != "") {
            todos.add(TodoDataObject(path, currentStructure, currentStructureType, currentFeedback.removeSuffix("\n")))
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
