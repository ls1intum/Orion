package de.tum.www1.orion.exercise

import com.intellij.openapi.util.io.FileUtil
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.util.OrionFileUtils
import de.tum.www1.orion.util.PropertiesUtil
import java.io.File

object OrionJavaInstructorProjectCreator {
    private const val BASE_TEMPLATE_PATH = "template/instructor_project"

    fun prepareProjectForImport(baseDir: File) {
        val ideaDirectory = File(baseDir, ".idea")
        FileUtil.ensureExists(ideaDirectory)

        setupProjectIml(ideaDirectory, baseDir)
        setupProjectMisc(ideaDirectory)
        setupProjectModules(ideaDirectory, baseDir)
        setupProjectVcs(ideaDirectory)
        listOf(RepositoryType.TEMPLATE, RepositoryType.SOLUTION, RepositoryType.TEST).forEach { setupSubmodule(baseDir, it) }
    }

    private fun setupSubmodule(baseDir: File, type: RepositoryType) {
        val moduleDirectory = File(baseDir, type.directoryName)
        val moduleIml = File(moduleDirectory, type.directoryName + ".iml")
        FileUtil.createIfNotExists(moduleIml)
        var template = loadTemplate("module", "iml")
        template = replaceWithProperties(template, "module", "java", "iml.properties")
        val sourcesRoot = if (type == RepositoryType.TEST) "/test" else "/src"
        template = template.replace("#sourcesRoot", sourcesRoot)
        template = template.replace("#isTestSource", (type == RepositoryType.TEST).toString())
        moduleIml.writeText(template)
    }

    private fun setupProjectVcs(ideaDirectory: File) {
        val projectVcs = File(ideaDirectory, "vcs.xml")
        FileUtil.createIfNotExists(projectVcs)
        val template = loadTemplate("idea", "vcs.xml")
        projectVcs.writeText(template)
    }

    private fun setupProjectModules(ideaDirectory: File, baseDir: File) {
        val projectModules = File(ideaDirectory, "modules.xml")
        FileUtil.createIfNotExists(projectModules)
        var template = loadTemplate("idea", "modules.xml")
        template = template.replace("#baseDir", baseDir.name)
        projectModules.writeText(template)
    }

    private fun setupProjectMisc(ideaDirectory: File) {
        val projectMisc = File(ideaDirectory, "misc.xml")
        FileUtil.createIfNotExists(projectMisc)
        val template = loadTemplate("idea", "misc.xml")
        projectMisc.writeText(template)
    }

    private fun setupProjectIml(ideaDirectory: File, baseDir: File) {
        val projectIml = File(ideaDirectory, "${baseDir.name}.iml")
        FileUtil.createIfNotExists(projectIml)
        val template = loadTemplate("idea", "iml")
        projectIml.writeText(replaceWithProperties(template, "idea", "java", "iml.properties"))
    }

    private fun loadTemplate(vararg pathComponents: String): String {
        return OrionFileUtils.systemIndependentPathOf(BASE_TEMPLATE_PATH, *pathComponents).let {
            javaClass.classLoader.getResource(it)?.readText()
                    ?: throw IllegalArgumentException("Artemis template does not exist in path $it")
        }
    }

    private fun replaceWithProperties(template: String, vararg pathComponents: String): String {
        var patched = template
        val props = PropertiesUtil.readProperties(OrionFileUtils.systemIndependentPathOf(BASE_TEMPLATE_PATH, *pathComponents))
        props.forEach{ patched = patched.replace("#" + it.key, it.value.toString()) }

        return patched
    }
}
