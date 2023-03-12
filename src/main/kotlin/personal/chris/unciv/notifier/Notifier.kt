package personal.chris.unciv.notifier

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Notifier {

    val client = HttpClient.newBuilder().build()
    val token = System.getenv("DISCORD_TOKEN")

    val CHANNEL_ID = "1084233698439876649"
    val CHANNEL_MESSAGE_SEND_URL = URI.create("https://discord.com/api/v9/channels/$CHANNEL_ID/messages")

    init {
        // Load some config from a file

    }

    fun notify(target: String) {
        println("Notifying ${target} it is their turn")

        // Target here sohuld be a user ID, eg

        val messageBody = """
            {
                "content": "It is your turn <@${target}>!"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(CHANNEL_MESSAGE_SEND_URL)
            .POST(HttpRequest.BodyPublishers.ofString(messageBody))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bot ${token}")
            .build();

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            println("Error sending notification: ${response.body()}")
            throw RuntimeException("Error sending notification");
        } else {
            println("Notification sent to ${target}")
        }
    }

    // TODO we could also regularly check how long it's been the current player's turn, and trigger some extra notification
}