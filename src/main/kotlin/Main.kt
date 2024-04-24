import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "串口调试工具",
        icon = painterResource("logo.png"),
        undecorated = false,
        transparent = false,
        resizable = false,
        alwaysOnTop = false,
    ) {
        App()
    }
}
