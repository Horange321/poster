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
        when (x.toInt()) {
            0 -> {
                name = "GET"
                color = Color(0, 0x99, 0)
            }

            1 -> {
                name = "POST"
                color = Color(0xff, 0xde, 0x66)
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
                else -> throw RuntimeException("Unknown method: $x")
            }
        )
    }
}