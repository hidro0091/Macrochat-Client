package org.leah.macrochatNaCl

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import javafx.scene.input.KeyCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.URI
import kotlin.jvm.java
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

const val WIDTH = 800.0
const val HEIGHT = 600.0
const val CURRENT_VERSION = "2.1.0"

val client = HttpClient(CIO) {
    install(WebSockets) {
        pingIntervalMillis = 15.seconds.inWholeMilliseconds
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
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

val lightTheme: String? = App::class.java.getResource("/light.css")!!.toExternalForm()
val darkTheme: String? =  App::class.java.getResource("/dark.css")!!.toExternalForm()
var isDark: Boolean = false

fun setDarkMode(scene: Scene, dark: Boolean) {
    scene.stylesheets.clear()

    scene.stylesheets.add(
        if (dark) darkTheme else lightTheme
    )
}

fun isHttps(url: String): Boolean {
    return url.startsWith("https://")
}

fun createThemeButton(stage: Stage): Button {
    val button = Button(if (isDark) "Light Mode" else "Dark Mode")

    button.setOnAction {
        isDark = !isDark
        button.text = if (isDark) "Light Mode" else "Dark Mode"
        setDarkMode(stage.scene, isDark)
    }

    return button
}

fun chat(host: String, user: String, stage: Stage): VBox {
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

    val buttons = HBox()

    val disconnect = Button("Disconnect")
    disconnect.setOnAction {
        stage.scene.root = connect(stage)
    }

    val toggleDark = createThemeButton(stage)

    VBox.setVgrow(messages, Priority.ALWAYS)

    val spacer = Region()
    HBox.setHgrow(spacer, Priority.ALWAYS)

    buttons.children.addAll(disconnect, spacer, toggleDark)

    val connected = Label("Connected to $host as $user")
    chat.children.addAll(connected, messages, input, buttons)
    chat.spacing = 10.0
    return chat
}

@Serializable
data class GithubRelease(
    val tag_name: String
)

suspend fun getLatestRelease(): GithubRelease {
    return client.get(
        "https://api.github.com/repos/hidro0091/Macrochat-Client/releases/latest"
    ).body()
}

fun connect(stage: Stage): VBox {
    val connect = VBox()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    scope.launch {
        try {
            val release = getLatestRelease()

            if (release.tag_name != CURRENT_VERSION) {
                Platform.runLater {
                    val viewRelease = ButtonType("Take me there!")
                    val ok = ButtonType("I'll do it myself.")

                    val alert = Alert(
                        Alert.AlertType.INFORMATION,
                        "Macrochat NaCl ${release.tag_name} is here.\nPlease download it from the Github!",
                        viewRelease, ok

                    )
                    alert.title = "Update!!"
                    alert.headerText = "There's a new version of Macrochat NaCl."

                    val result = alert.showAndWait()
                    if (result.orElse(ok) == viewRelease) {
                        Desktop.getDesktop().browse(
                            URI("https://github.com/hidro0091/Macrochat-Client/releases/latest")
                        )
                    }
                    Platform.exit()
                }
            }
        } catch (_: Exception) {
            println("nope")
        }
    }

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

    val buttons = HBox()

    val buttonConnect = Button("Connect")
    buttonConnect.setOnAction {
        val url = hField.text
        val username = uField.text
        stage.scene.root = chat(url, username, stage)
    }

    val spacer = Region()
    HBox.setHgrow(spacer, Priority.ALWAYS)

    val toggleDark = createThemeButton(stage)

    buttons.children.addAll(buttonConnect, spacer, toggleDark)

    connect.children.addAll(host, user, buttons)
    return connect
}

class App : Application() {
    override fun start(stage: Stage) {
        val scene = Scene(connect(stage), WIDTH, HEIGHT)
        setDarkMode(scene, isDark)

        stage.title = "Macrochat NaCl"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(App::class.java)
}