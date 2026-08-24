package org.leah.macrochatNaCl

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Parameters
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import javafx.scene.input.KeyCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

const val WIDTH = 800.0
const val HEIGHT = 600.0

val client = HttpClient(CIO) {
    install(WebSockets) {
        pingIntervalMillis = 15.seconds.inWholeMilliseconds
    }
}

suspend fun postMessage(
    host: String,
    username: String,
    message: String,
) {
    client.post {
        url(host)
        setBody(
            FormDataContent(
                Parameters.build {
                    append("username", username)
                    append("time", Clock.System.now().toEpochMilliseconds().toString())
                    append("contents", message)
                }
            )
        )
    }
}

fun isHttps(url: String): Boolean {
    return url.startsWith("https://")
}

fun chat(host: String, user: String): VBox {
    val chat = VBox()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val webSocketUrl = "${if (isHttps(host)) "wss" else "ws"}://${host.removePrefix("https://").removePrefix("http://")}/chat"
    val messages = TextArea()
    messages.isEditable = false

    scope.launch {
        client.webSocket(webSocketUrl) {
            var newMessages = ""
            while (isActive) {
                val frame = incoming.receive()

                if (frame is Frame.Text) {
                    newMessages = frame.readText()
                }

                Platform.runLater {
                    messages.text = newMessages
                    messages.positionCaret(messages.length)
                }
            }
        }
    }

    val input = TextArea()
    input.setOnKeyPressed { event ->
        if (event.code == KeyCode.ENTER) {
            if (!event.isShiftDown) {
                event.consume()

                val message = input.text.trim()

                if (message.isNotBlank()) {
                    scope.launch {
                        postMessage(host, user, message)
                    }

                    input.clear()
                }
            } else {
                input.text += "\n"
                input.positionCaret(input.text.length)
            }
        }
    }

    val connected = Label("Connected to $host as $user")
    chat.children.addAll(connected, messages, input)
    return chat
}
fun connect(stage: Stage): VBox {
    val connect = VBox()

    val host = HBox()
    val hLabel =
        Label("Enter the URL of the server you are attempting to access. (e.g. 'https://example.com', 'http://127.0.0.1:8080/')'")
    val hField = TextField()
    host.children.addAll(hLabel, hField)
    host.spacing = 10.0

    val user = HBox()
    val uLabel =
        Label("Enter your display name (the name you want other users of the server to see)")
    val uField = TextField()
    user.children.addAll(uLabel, uField)
    user.spacing = 10.0

    val buttonConnect = Button("Connect")
    buttonConnect.setOnAction {
        val url = hField.text
        val username = uField.text
        stage.scene = Scene(chat(url, username), WIDTH, HEIGHT)
    }

    connect.children.addAll(host, user, buttonConnect)
    return connect
}

class App : Application() {
    override fun start(stage: Stage) {
        val scene = Scene(connect(stage), WIDTH, HEIGHT)
        stage.title = "Macrochat NaCl"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(App::class.java)
}