package org.leah.macrochat

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.Parameters
import kotlinx.coroutines.*
import kotlin.time.Clock
import io.ktor.client.plugins.websocket.*
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlin.time.Duration.Companion.seconds

enum class Screen {
    Connect,
    Chat
}

@Composable
fun Connect(
    onConnect: (host: String, username: String) -> Unit,
) {
    var host by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Column {
        Text("Where are we going? (Enter the host URL, something like 'https://example.com' or 'http://0.0.0.0:8080')")
        TextField(
            value = host,
            onValueChange = { host = it },
            singleLine = true,
        )
        Text("Your username (Enter what you want others to see)")
        TextField(
            value = username,
            onValueChange = { username = it },
            singleLine = true,
        )

        Button(
            onClick = {
                onConnect(host.trim(), username)
            }
        ) {
            Text("Join!")
        }
    }
}

val client = HttpClient(CIO) {
    install(WebSockets) {
        pingIntervalMillis = 15.seconds.inWholeMilliseconds
    }
}

suspend fun postMessage(
    host: String,
    username: String,
    message: String
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

@Composable
fun Chat(user: String, host: String) {
    var messages by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(host) {
        launch {
            client.webSocket(
                urlString = "wss://${host.removePrefix("https://".removePrefix("http://"))}/chat",
            ) {
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        messages = frame.readText()
                    }
                }
            }
        }
    }

    Column {
        Text("Connected to $host as $user")
        Text(messages)
        TextField(
            value = message,
            onValueChange = { message = it },
        )
        Button(
            onClick = {
                scope.launch {
                    postMessage(
                        host = host,
                        username = user,
                        message = message
                    )

                    message = ""
                }
            }
        ) {
            Text("Send")
        }
    }
}

@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.Connect) }

    var host by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }

    when (screen) {
        Screen.Connect -> Connect(
            onConnect = { newHost, newUser ->
                host = newHost
                user = newUser
                screen = Screen.Chat
            }
        )

        Screen.Chat -> Chat(
            user = user,
            host = host
        )
    }
}