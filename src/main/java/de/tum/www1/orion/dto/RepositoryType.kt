package de.tum.www1.orion.dto

enum class RepositoryType(val directoryName: String) {
    ASSIGNMENT("assignment"),
    TEST("tests"), SOLUTION("solution"), TEMPLATE("exercise");
}