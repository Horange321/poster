package horange.poster.pojo

import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.Properties

data class Api(
    var title: String,
    var desc: String,
) : Serializable {
    var method: Int = 0
    var url = "http://localhost:8080/"
    val params = Properties()
    val headers = Properties()
    var body = "{\n\t\n}"

    init {
        headers["Content-Type"] = "application/json"
        headers["User-Agent"] = "PosterRuntime"

    }
}

fun Properties.toPropertyString(comments: String): String {
    val r = ByteArrayOutputStream()
    store(r, comments)
    return r.toString()
}