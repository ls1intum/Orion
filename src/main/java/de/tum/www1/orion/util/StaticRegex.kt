package de.tum.www1.orion.util

/**
 * A Companion Object to hold some reusable regex expressions
 */
class StaticRegex {
    companion object {
        val JAVA_METHOD_REGEX =
            Regex("\\s*\\b(?:public|private|protected|static|final|\\s)*\\s+\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*.*")
        val JAVA_TODO_REGEX = Regex(".*(TODO.?\\s).*")
        val JAVA_TODO_PREFIX = Regex(".*(TODO.?\\s)")
        val SLASH_SLASH_COMMENT_REGEX = Regex("//.*")
    }
}
