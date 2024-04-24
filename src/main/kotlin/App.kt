import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.desktop.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var log by remember { mutableStateOf("") }
        val scrollState = rememberScrollState()
        var ports by remember { mutableStateOf(listOf<SerialPort>()) }
        var listName by remember { mutableStateOf(listOf("")) }
        var selectName by remember { mutableStateOf("") }
        var selectSP by remember { mutableStateOf<SerialPort>(SerialPort.getCommPort("")) }
        var isOpen by remember { mutableStateOf(false) }
        val baudList = listOf(1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 230400, 460800, 921600)
        var selectBaud by remember { mutableStateOf("115200") }
        var send by remember { mutableStateOf("") }
        var tx by remember { mutableStateOf(0L) }
        var rx by remember { mutableStateOf(0L) }
        var hexTx by remember { mutableStateOf(false) }
        var hexRx by remember { mutableStateOf(false) }
        var hex0D0A by remember { mutableStateOf(false) }
        fun log(message: String) {
            log = "${log}${message}\n"
            scope.launch(Dispatchers.IO) { scrollState.scrollTo(scrollState.maxValue) }
        }
        LaunchedEffect(Unit) {
            ports = SerialPort.getCommPorts().toList()
            listName = ports.map { it.systemPortName }.sorted().toMutableList()
            kotlin.runCatching {
                selectName = ports[0].systemPortName
                selectSP = ports.first { it.systemPortName == selectName }
            }
            Thread {
                val readBuffer = ByteArray(2048)
                while (true) {
                    if (isOpen) {
                        val length = selectSP.readBytes(readBuffer, readBuffer.size)
                        if (length > 0) {
                            log(String(readBuffer, 0, length))
                            rx += length
//                            if (hexRx) {
//                            } else {
//                                log(String(readBuffer, 0, length))
//                            }
                        }
                    }
                    Thread.sleep(100)
                }
            }.start()
        }
        Column(modifier = Modifier.fillMaxSize().padding(5.dp)) {
            Box(modifier = Modifier.fillMaxWidth().weight(7f)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("运行日志:", fontSize = 12.sp)
                        Text("tx:${tx},rx:${rx}", fontSize = 12.sp)
                        TextButton(
                            modifier = Modifier.size(40.dp, 30.dp),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(vertical = 3.dp),
                            onClick = {
                                log = ""
                                tx = 0
                                rx = 0
                            },
                        ) { Text(text = "清空", fontSize = 12.sp, color = Color.Black) }
                    }
                    OutlinedTextField(
                        value = log,
                        onValueChange = { log = it },
                        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                        textStyle = LocalTextStyle.current,
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().weight(3f).padding(top = 1.dp)
                    .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(3))
            ) {
                Column(modifier = Modifier.padding(3.dp)) {
                    Row {
                        MySpinner(title = "串口号", listName, selectName) { name ->
                            selectName = name
                            selectSP = ports.first { it.systemPortName == selectName }
                        }
                        MySpinner(title = "波特率", baudList.map { "$it" }, selectBaud) { selectBaud = it }
                        Button(onClick = {
                            if (isOpen) {
                                isOpen = !selectSP.closePort()
                            } else {
                                isOpen = selectSP.openPort()
                                if (isOpen) log(selectSP.systemPortName) else log("打开失败")
                                selectSP.setComPortParameters(selectBaud.toInt(), 8, 1, 0)
                                selectSP.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 1000, 0)
                            }
                        }) { Text(if (isOpen) "关闭" else "打开") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(text = "Hex显示")
                        Checkbox(checked = hexRx, onCheckedChange = { hexRx = it })
                        Text(text = "Hex发送")
                        Checkbox(checked = hexTx, onCheckedChange = {
                            hexTx = it
                            send = if (it) send.toByteArray().toHexString()
                            else String(send.hexToByteArray())
                        })
                        Text(text = "帧换行")
                        Checkbox(checked = hex0D0A, onCheckedChange = { hex0D0A = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        OutlinedTextField(value = send, onValueChange = { send = it })
                        Button(
                            onClick = {
                                try {
                                    var sendByte = if (hexTx) send.hexToByteArray() else send.toByteArray()
                                    if (hex0D0A) sendByte += byteArrayOf(0x0D, 0x0A)
                                    val result = selectSP.writeBytes(sendByte, sendByte.size)
                                    tx += result
                                } catch (e: Exception) {
                                    log("数据转换异常:${e.message}")
                                    e.printStackTrace()
                                }
                            },
                            enabled = isOpen
                        ) {
                            Text(text = "发送")
                        }
                    }
                }
            }
        }
    }
}