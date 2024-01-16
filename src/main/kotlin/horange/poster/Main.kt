package horange.poster

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File

fun main(args: Array<String>) = application(exitProcessOnExit = true) {
    val app = App(File(if (args.isNotEmpty()) args[0] else "apis"))
    Window(
        onCloseRequest = ::exitApplication,
        title = "Poster"
    ) {
        app.Win()
    }
}
