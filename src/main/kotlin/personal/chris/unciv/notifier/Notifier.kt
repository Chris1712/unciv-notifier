package personal.chris.unciv.notifier

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

class Notifier(config: Config) {

    private val client: HttpClient
    private val token: String
    private val channelMessageSendUri: URI
    private val uncivToDiscordUserMap: Map<UUID, String>

    init {
        this.client = HttpClient.newBuilder().build()
        // Load some config from a file
        this.token = config.discordToken
        this.channelMessageSendUri = URI.create("https://discord.com/api/v9/channels/${config.discordChannelId}/messages")!!
        this.uncivToDiscordUserMap = config.uncivToDiscordUserMap
    }

    fun notify(target: UUID) {
        println("Notifying unciv uuid ${target} that it is their turn")
        val targetDiscordId = uncivToDiscordUserMap[target]
        if (targetDiscordId == null) {
            println("No discord user mapped to unciv uuid ${target} - check config file")
            return
        } else {
            println("Mapped unciv uuid ${target} to discord user id ${targetDiscordId}")
        }

        val messageBody = """
            {
                "content": "It is your turn <@${targetDiscordId}>!"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(channelMessageSendUri)
            .POST(HttpRequest.BodyPublishers.ofString(messageBody))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bot ${token}")
            .build();

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            println("Error sending notification: ${response.body()}")
            throw RuntimeException("Error sending notification to unciv uuid ${target}, discord user id ${targetDiscordId}");
        } else {
            println("Notification sent to unciv uuid ${target}, discord user id ${targetDiscordId}")
        }
    }
}
