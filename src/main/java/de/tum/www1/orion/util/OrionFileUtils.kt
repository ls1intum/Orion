package de.tum.www1.orion.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.SystemIndependent
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * Provides utility functions associated with files, like finding paths, storing data as file or unzipping files
 */
object OrionFileUtils {
    /**
     * Generates a path with the given prefix that does not yet exist by appending an arbitrary number to it
     *
     * @param prefix the path to get a nonexistent version of
     * @return the path with an arbitrary number appended, guaranteed to not exist yet
     */
    fun getUniqueFilename(prefix: Path, ending: String): Path {
        var counter = 0
        var result = Paths.get("$prefix$ending")
        while (Files.exists(result)) {
            result = Paths.get("$prefix${counter++}$ending")
        }
        return result
    }

    /**
     * Extracts all files from the given zip file to the given destination directory
     *
     * @param source path to zip to be unzipped
     * @param destination path to directory to unzip into
     */
    fun unzip(source: Path, destination: Path) {
        ZipFile(source.toFile()).use { file ->
            for (entry in file.entries()) {
                val to = destination.resolve(entry.name)
                unzipEntry(file, entry, to)
            }
        }
    }

    /**
     * Extracts a single entry from a zip file to the given destination
     *
     * @param file zip file to extract from
     * @param entry to extract
     * @param destination file to store the data in
     */
    fun unzipEntry(file: ZipFile, entry: ZipEntry, destination: Path) {
        if (!entry.isDirectory) {
            Files.createDirectories(destination.parent)

            FileOutputStream(destination.toFile()).use {
                it.channel.transferFrom(Channels.newChannel(file.getInputStream(entry)), 0, Long.MAX_VALUE)
            }
        }
    }

    /**
     * Verifies the given source contains exactly 1 entry, then extracts that entry to the given destination
     *
     * @param source zip file to extract from
     * @param destination file to store the data in
     */
    fun unzipSingleEntry(source: Path, destination: Path) {
        ZipFile(source.toFile()).use {
            if (it.size() != 1) {
                throw ZipException("Expected 1 entry, but found ${it.size()}")
            }

            unzipEntry(it, it.entries().nextElement(), destination)
        }
    }

    /**
     * Decodes the given data and writes them to the file represented by the given path
     *
     * @param base64data data to decode and write
     * @param destination file to write into
     */
    fun storeBase64asFile(base64data: String, destination: Path) {
        FileOutputStream(destination.toFile()).use {
            it.write(Base64.getDecoder().decode(base64data))
        }
    }

    /**
     * Deletes the given path if needed
     *
     * @param toDelete path to delete
     * @return true if successful or path does not exist, false otherwise
     */
    fun deleteIfExists(toDelete: Path): Boolean {
        if (Files.exists(toDelete)) {
            return toDelete.toFile().deleteRecursively()
        }
        return true
    }

    /**
     * Creates a system independent (= /-separated) string of the given path and path components
     *
     * @param basePath of the new path
     * @param pathComponents to append to the base path
     * @return the base path and components, concatenated with slashes
     */
    fun systemIndependentPathOf(basePath: @SystemIndependent String, vararg pathComponents: String): String {
        return basePath + "/" + pathComponents.joinToString("/")
    }

    fun getRoot(project: Project): VirtualFile? {
        if (project.basePath != null) {
            val lfs = LocalFileSystem.getInstance()
            return lfs.findFileByPath(project.basePath!!)
        }
        return null
    }
}
