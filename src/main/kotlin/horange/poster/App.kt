package horange.poster

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import horange.poster.pojo.Api
import horange.poster.pojo.MethodUI
import horange.poster.pojo.toPropertyString
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.logging.Logger
import javax.swing.JOptionPane
import kotlin.collections.ArrayList

class App(private val f: File) {
    private val log = Logger.getLogger("App")
    private var apis = arrayListOf(Api("Api A", "Description"))
    private val http = OkHttpClient()


    init {
        if (f.exists()) {
            try {
                f.inputStream().let { s ->
                    apis = ObjectInputStream(s).readObject() as ArrayList<Api>
                    s.close()
                }
            } catch (e: Exception) {
                Thread {
                    JOptionPane.showMessageDialog(
                        null,
                        "Read error on file ${f.absolutePath}\nUsing default",
                        "File error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }.start()
                log.severe(e.stackTraceToString())
            }
        }

    }


    @OptIn(ExperimentalFoundationApi::class)
    @Preview
    @Composable
    fun Win() {
        val api0 = apis[0]
        var api by remember { mutableStateOf(0) }
        var te_title by remember { mutableStateOf(api0.title) }
        var dd_method_exp by remember { mutableStateOf(false) }
        var method by remember { mutableStateOf(MethodUI(api0.method)) }
        var te_url by remember { mutableStateOf(api0.url) }
        var bu_send by remember { mutableStateOf(true) }
        var te_headers by remember { mutableStateOf(api0.headers.toPropertyString("headers")) }
        var te_params by remember { mutableStateOf(api0.params.toPropertyString("params")) }
        var te_body by remember { mutableStateOf(api0.body) }
        var te_resp by remember { mutableStateOf("") }
        var te_resp_err by remember { mutableStateOf(false) }
        var dia_new by remember { mutableStateOf(false) }
        var dia_edit by remember { mutableStateOf(false) }
        var t1 = Date().time


        Row(Modifier.fillMaxSize()) {
            //左侧API栏
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Text(
                    "APIS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color(0xff, 0x8a, 0))
                        .fillMaxWidth()
                        .onClick {
                            dia_new = true
                        }
                )
                apis.forEachIndexed { i, v ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .shadow(2.dp)
                            .background(if (i == api) Color(0xff, 0xcf, 0x9b) else Color.White)
                            .onClick(onLongClick = {
                                api = i
                                dia_edit = true
                            }) {
                                api = i
                                te_title = v.title
                                te_url = v.url
                                method = MethodUI(v.method)
                                te_headers = v.headers.toPropertyString("headers")
                                te_params = v.params.toPropertyString("params")
                                te_body = v.body
                            }
                    ) {
                        Text(
                            apis[i].title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            apis[i].desc,
                            fontSize = 16.sp,
                        )
                    }
                }
            }

            //右侧
            Column(
                Modifier
                    .weight(4f)
            ) {
                //te_title
                Text(
                    te_title,
                    color = Color.White,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .background(Color(0xff, 0x8a, 0))
                        .fillMaxWidth()
                        .onClick {
                            apis[api].let {
                                it.method = method.id
                                it.url = te_url
                                it.headers.clear()
                                it.headers.load(te_headers.byteInputStream())
                                it.params.clear()
                                it.params.load(te_params.byteInputStream())
                                it.body = te_body
                            }
                            f.outputStream().let { s ->
                                ObjectOutputStream(s).writeObject(apis)
                                s.close()
                            }
                            JOptionPane.showMessageDialog(
                                null,
                                "Saved"
                            )
                        }
                )

                //上部编辑区
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    //url栏
                    Row {
                        //dd_method
                        Column {
                            TextButton(
                                { dd_method_exp = true },
                            ) {
                                Text(
                                    method.name + ' ',
                                    fontSize = 32.sp,
                                    color = method.color
                                )
                            }
                            DropdownMenu(
                                dd_method_exp,
                                { dd_method_exp = false }
                            ) {
                                for (i in 0..5) {
                                    val m = MethodUI(i)
                                    TextButton(
                                        {
                                            dd_method_exp = false
                                            method = MethodUI(i)
                                        },
                                        modifier = Modifier
                                    ) {
                                        Text(
                                            m.name,
                                            fontSize = 32.sp,
                                            color = m.color,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        //te_url
                        TextField(
                            te_url,
                            {
                                te_url = it
                            },
                            maxLines = 1,
                            label = {
                                Text("Url")
                            },
                            modifier = Modifier
                                .weight(1f)
                        )

                        //bu_send
                        Button(
                            {
                                bu_send = false
                                val url = te_url.toHttpUrl().newBuilder()
                                Properties().let {
                                    it.load(te_params.byteInputStream())
                                    it.forEach { p ->
                                        url.addQueryParameter(p.key as String, p.value as String)
                                    }
                                }

                                val req = Request.Builder()
                                    .url(url.build())
                                Properties().let {
                                    it.load(te_headers.byteInputStream())
                                    it.forEach { h ->
                                        req.header(h.key as String, h.value as String)
                                    }
                                }

                                val rb = te_body.toRequestBody("application/json".toMediaType())

                                http.newCall(
                                    when (method.id) {
                                        0 -> req
                                            .get()
                                            .build()

                                        1 -> req
                                            .post(rb)
                                            .build()

                                        2 -> req
                                            .put(rb)
                                            .build()

                                        3 -> req
                                            .patch(rb)
                                            .build()

                                        4 -> req
                                            .delete(rb)
                                            .build()

                                        5 -> req
                                            .head()
                                            .build()

                                        else -> throw RuntimeException("Unknown method: ${method.name}")
                                    }
                                ).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        log.warning(e.stackTraceToString())
                                        te_resp_err = true
                                        te_resp = e.stackTraceToString()
                                        bu_send = true
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val t2 = Date().time
                                        te_resp_err = false
                                        val sb = StringBuilder()
                                        sb.append(response.code.toString())
                                        sb.append("   ")
                                        sb.append(t2 - t1)
                                        sb.append("ms   ")
                                        val resp = response.body?.string() ?: "No response body"
                                        response.close()
                                        sb.append(resp.length / 1000)
                                        sb.append("kB\n")
                                        sb.append(resp)
                                        te_resp = sb.toString()
                                        bu_send = true
                                    }
                                })
                                t1 = Date().time
                            },
                            enabled = bu_send
                        ) {
                            Text(
                                "Send",
                                fontSize = 30.sp
                            )
                        }
                    }

                    //中部参数区
                    Row(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        //te_headers
                        TextField(
                            te_headers,
                            { te_headers = it },
                            Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        )

                        //te_params
                        TextField(
                            te_params,
                            { te_params = it },
                            Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        )

                        //te_body
                        TextField(
                            te_body,
                            { te_body = it },
                            label = { Text("Body") },
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        )
                    }
                }

                //下部输出栏
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    //te_resp
                    TextField(
                        te_resp,
                        {},
                        readOnly = true,
                        label = { Text("Response") },
                        isError = te_resp_err,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }

        if (dia_new) {
            var te_new_title by remember { mutableStateOf("") }
            var te_new_des by remember { mutableStateOf("") }
            AlertDialog({ dia_new = false },
                title = { Text("New API") },
                text = {
                    Column {
                        //te_new_title
                        TextField(
                            te_new_title,
                            { te_new_title = it },
                            label = { Text("Title") },
                            maxLines = 1
                        )

                        //te_new_desc
                        TextField(
                            te_new_des,
                            { te_new_des = it },
                            label = { Text("Description") }
                        )
                    }
                },
                confirmButton = {
                    Button({
                        dia_new = false
                        apis.add(Api(te_new_title, te_new_des))
                    }) {
                        Text("OK")
                    }
                }
            )
        }

        if (dia_edit) {
            val api1 = apis[api]
            var te_new_title by remember { mutableStateOf(api1.title) }
            var te_new_des by remember { mutableStateOf(api1.desc) }
            AlertDialog(
                { dia_edit = false },
                title = { Text("Edit API") },
                text = {
                    Column {
                        //te_new_title
                        TextField(
                            te_new_title,
                            { te_new_title = it },
                            label = { Text("Title") },
                            maxLines = 1
                        )

                        //te_new_desc
                        TextField(
                            te_new_des,
                            { te_new_des = it },
                            label = { Text("Description") }
                        )
                    }
                },
                confirmButton = {
                    Button({
                        api1.title = te_new_title
                        api1.desc = te_new_des
                        api = 0
                        dia_edit = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button({
                        apis.removeAt(api)
                        api = 0
                        dia_edit = false
                    }) {
                        Text(
                            "Remove",
                            color = Color.Red
                        )
                    }
                }
            )
        }
    }
}