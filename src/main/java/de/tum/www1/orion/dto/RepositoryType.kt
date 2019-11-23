package de.tum.www1.orion.dto

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

enum class RepositoryType(val directoryName: String) {
    ASSIGNMENT("assignment"),
    TEST("tests"), SOLUTION("solution"), TEMPLATE("exercise");

    fun moduleIn(project: Project): Module? {
        return ModuleManager.getInstance(project).findModuleByName(directoryName)
    }
}