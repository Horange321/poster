package horange.poster

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

    private object vm {
        var api = mutableStateOf(0)
        var te_title = mutableStateOf("")
        var dd_method_exp = mutableStateOf(false)
        var method = mutableStateOf(MethodUI(0))
        var te_url = mutableStateOf("")
        var bu_send = mutableStateOf(true)
        var te_headers = mutableStateOf("")
        var te_params = mutableStateOf("")
        var te_body = mutableStateOf("")
        var te_resp = mutableStateOf("")
        var te_resp_err = mutableStateOf(false)
        var dia_new = mutableStateOf(false)
        var dia_edit = mutableStateOf(false)
    }


    init {
        //读取保存的
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


    @Preview
    @Composable
    fun Win() {
        val api0 = apis[0]
        vm.te_title.value = api0.title
        vm.te_url.value = api0.url
        vm.te_headers.value = api0.headers.toPropertyString("headers")
        vm.te_params.value = api0.params.toPropertyString("params")
        vm.te_body.value = api0.body

        Row(Modifier.fillMaxSize()) {
            //左侧API栏
            Column(Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
                tv_api()
                apis.forEachIndexed { i, v ->
                    col_api(i, v)
                }
            }

            //右侧
            Column(Modifier.weight(4f)) {
                te_title()

                //上部编辑区
                Column(Modifier.weight(1f).fillMaxWidth()) {
                    //url栏
                    Row {
                        dd_method()
                        te_url(Modifier.weight(1f))
                        bu_send()
                    }

                    //中部参数区
                    Row(Modifier.fillMaxSize()) {
                        te_headers(Modifier.weight(1f))
                        te_params(Modifier.weight(1f))
                        te_body(Modifier.weight(1f))
                    }
                }

                //下部输出栏
                Column(Modifier.weight(1f).fillMaxWidth()) {
                    te_resp()
                }
            }
        }

        if (vm.dia_new.value) {
            dia_new()
        }
        if (vm.dia_edit.value) {
            dia_edit()
        }
    }


    @Composable
    private fun dia_edit() {
        val api1 = apis[vm.api.value]
        var te_new_title by remember { mutableStateOf(api1.title) }
        var te_new_des by remember { mutableStateOf(api1.desc) }

        AlertDialog(
            { vm.dia_edit.value = false },
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
                    vm.api.value = 0
                    vm.dia_edit.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button({
                    apis.removeAt(vm.api.value)
                    vm.api.value = 0
                    vm.dia_edit.value = false
                }) {
                    Text(
                        "Remove",
                        color = Color.Red
                    )
                }
            }
        )
    }


    @Composable
    private fun dia_new() {
        var te_new_title by remember { mutableStateOf("") }
        var te_new_des by remember { mutableStateOf("") }

        AlertDialog({ vm.dia_new.value = false },
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
                    vm.dia_new.value = false
                    apis.add(Api(te_new_title, te_new_des))
                }) {
                    Text("OK")
                }
            }
        )
    }


    @Composable
    private fun te_resp() {
        TextField(
            vm.te_resp.value,
            {},
            readOnly = true,
            label = { Text("Response") },
            isError = vm.te_resp_err.value,
            modifier = Modifier.fillMaxSize()
        )
    }


    @Composable
    private fun te_body(modifier: Modifier) {
        TextField(
            vm.te_body.value,
            { vm.te_body.value = it },
            label = { Text("Body") },
            modifier = modifier
        )
    }


    @Composable
    private fun te_params(modifier: Modifier) {
        TextField(
            vm.te_params.value,
            { vm.te_params.value = it },
            modifier
        )
    }


    @Composable
    private fun te_headers(modifier: Modifier) {
        TextField(
            vm.te_headers.value,
            { vm.te_headers.value = it },
            modifier
        )
    }


    @Composable
    private fun te_url(modifier: Modifier) {
        TextField(
            vm.te_url.value,
            {
                vm.te_url.value = it
            },
            maxLines = 1,
            label = {
                Text("Url")
            },
            modifier = modifier
        )
    }


    @Composable
    private fun bu_send() {
        Button(
            {
                vm.bu_send.value = false
                val url = vm.te_url.value.toHttpUrl().newBuilder()
                Properties().let {
                    it.load(vm.te_params.value.byteInputStream())
                    it.forEach { p ->
                        url.addQueryParameter(p.key as String, p.value as String)
                    }
                }

                val req = Request.Builder()
                    .url(url.build())
                Properties().let {
                    it.load(vm.te_headers.value.byteInputStream())
                    it.forEach { h ->
                        req.header(h.key as String, h.value as String)
                    }
                }

                val rb = vm.te_body.value.toRequestBody("application/json".toMediaType())

                val t1 = Date().time
                http.newCall(
                    when (vm.method.value.id) {
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

                        else -> throw RuntimeException("Unknown method: ${vm.method.value.name}")
                    }
                ).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        log.warning(e.stackTraceToString())
                        vm.te_resp_err.value = true
                        vm.te_resp.value = e.stackTraceToString()
                        vm.bu_send.value = true
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val t2 = Date().time
                        vm.te_resp_err.value = false
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
                        vm.te_resp.value = sb.toString()
                        vm.bu_send.value = true
                    }
                })
            },
            enabled = vm.bu_send.value
        ) {
            Text(
                "Send",
                fontSize = 30.sp
            )
        }
    }


    @Composable
    private fun dd_method() {
        Column {
            TextButton(
                { vm.dd_method_exp.value = true },
            ) {
                Text(
                    vm.method.value.name + ' ',
                    fontSize = 32.sp,
                    color = vm.method.value.color
                )
            }
            DropdownMenu(
                vm.dd_method_exp.value,
                { vm.dd_method_exp.value = false }
            ) {
                for (i in 0..5) {
                    val m = MethodUI(i)
                    TextButton(
                        {
                            vm.dd_method_exp.value = false
                            vm.method.value = MethodUI(i)
                        },
//                        modifier = Modifier //不知道干嘛用的
                    ) {
                        Text(
                            m.name,
                            fontSize = 32.sp,
                            color = m.color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }


    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun te_title() {
        Text(
            vm.te_title.value,
            color = Color.White,
            fontSize = 32.sp,
            modifier = Modifier
                .background(Color(0xff, 0x8a, 0))
                .fillMaxWidth()
                .onClick {
                    apis[vm.api.value].let {
                        it.method = vm.method.value.id
                        it.url = vm.te_url.value
                        it.headers.clear()
                        it.headers.load(vm.te_headers.value.byteInputStream())
                        it.params.clear()
                        it.params.load(vm.te_params.value.byteInputStream())
                        it.body = vm.te_body.value
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
    }


    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun col_api(i: Int, v: Api) {
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(2.dp)
                .background(if (i == vm.api.value) Color(0xff, 0xcf, 0x9b) else Color.White)
                .onClick(onLongClick = {
                    vm.api.value = i
                    vm.dia_edit.value = true
                }) {
                    vm.api.value = i
                    vm.te_title.value = v.title
                    vm.te_url.value = v.url
                    vm.method.value = MethodUI(v.method)
                    vm.te_headers.value = v.headers.toPropertyString("headers")
                    vm.te_params.value = v.params.toPropertyString("params")
                    vm.te_body.value = v.body
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


    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun tv_api() {
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
                    vm.dia_new.value = true
                }
        )
    }
}