package horange.poster.pojo

import androidx.compose.ui.graphics.Color

class MethodUI {
    lateinit var name: String
        private set
    var color = Color(0, 0, 0)
        private set
    var id: Int = 0
        private set

    private fun build(x: Int) {
        id = x
        when (x) {
            0 -> {
                name = "GET"
                color = Color(0, 0x99, 0)
            }

            1 -> {
                name = "POST"
                color = Color(0xff, 0xde, 0x66)
            }

            2 -> {
                name = "PUT"
                color = Color(0x71, 0xae, 0xe9)
            }

            3 -> {
                name = "PATCH"
                color = Color(0x9c, 0x89, 0xb5)
            }

            4 -> {
                name = "DELETE"
                color = Color(0xd3, 0x86, 0x7c)
            }

            5 -> {
                name = "HEAD"
                color = Color(0x6b, 0xdd, 0x9a)
            }

            6 -> {
                name = "OPTIONS"
                color = Color(0xd4, 0x56, 0x9c)
            }

            else -> throw RuntimeException("Unknown method id: $x")
        }
    }

    constructor(x: Int) {
        build(x)
    }

    constructor(x: String) {
        build(
            when (x) {
                "GET" -> 0
                "POST" -> 1
                "PUT" -> 2
                "PATCH" -> 3
                "DELETE" -> 4
                "HEAD" -> 5
                "OPTIONS" -> 6
                else -> throw RuntimeException("Unknown method: $x")
            }
        )
    }
}