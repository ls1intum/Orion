package de.tum.www1.orion.enumeration

/**
 * Defines the programming language used in the exercise
 * Has to be synchronized with Artemis's ProgrammingLanguage enumeration
 *
 * Most values are unused, but still required, otherwise Orion will throw an exception
 * when downloading an exercise in that language
 */
enum class ProgrammingLanguage {
    JAVA, PYTHON, C, HASKELL, KOTLIN, VHDL, ASSEMBLER, SWIFT, OCAML, EMPTY
}
