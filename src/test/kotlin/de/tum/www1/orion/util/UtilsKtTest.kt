package de.tum.www1.orion.util

import org.junit.Test
import java.util.*

internal class UtilsKtTest {

    @Test
    fun nextAll() {
        val multiLineString = """
            onBuildStarted
            Param1
            line1
            line2
            line3
        """
        val scanner = Scanner(multiLineString)
        scanner.nextLine()
        assert(scanner.nextAll().also {
            println(it)
        } == multiLineString.lines().drop(1).joinToString("\n"))
    }
}