package de.tum.www1.orion.dto

import com.google.gson.annotations.SerializedName

data class BuildError(val row: Int, val column: Int, val text: String, val type: String, @SerializedName("ts") val timestamp: Long)

data class BuildLogFileErrors(val fileName: String, val errors: List<BuildError>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuildLogFileErrors

        if (fileName != other.fileName) return false
        if (errors != other.errors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + errors.hashCode()
        return result
    }
}