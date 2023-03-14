package personal.chris.unciv.notifier

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.Integer.max
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class Notifier(config: Config) {

    private val messagesToKeep = 5
    private val client: HttpClient = HttpClient.newBuilder().build()
    private val mapper: ObjectMapper = ObjectMapper()
    private val rnd: Random = Random()

    private val requestBase: (() -> HttpRequest.Builder)
    private val channelMessageUri: URI // For POST / GET message
    private val getDeleteUri: ((String) -> URI)
    private val uncivToDiscordUserMap: Map<UUID, String>

    init {
        this.requestBase = { HttpRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bot ${config.discordToken}") }
        this.channelMessageUri = URI.create("https://discord.com/api/v9/channels/${config.discordChannelId}/messages")!!
        this.getDeleteUri = { messageId ->
            URI.create("https://discord.com/api/v9/channels/${config.discordChannelId}/messages/${messageId}")!! }
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
                "content": "${getMessage(targetDiscordId)}"
            }
        """.trimIndent()

        val request = requestBase()
            .uri(channelMessageUri)
            .POST(HttpRequest.BodyPublishers.ofString(messageBody))
            .build();

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            println("Error sending notification: ${response.body()}")
            throw RuntimeException("Error sending notification to unciv uuid ${target}, discord user id ${targetDiscordId}");
        } else {
            println("Notification sent to unciv uuid ${target}, discord user id ${targetDiscordId}")
        }

        // Clean up old messages
        cleanMessages()
    }

    /**
     * Get a random message to send for turn notification
     */
    private fun getMessage(discordId: String): String {
        val messages = listOf(
            // With thanks to chatgpt
            "It's your turn, <@${discordId}>!",
            "<@${discordId}>, you're up!",
            "Time for your turn <@${discordId}>",
            "<@${discordId}>, the game needs you!",
            "Your turn, <@${discordId}>.",
            "<@${discordId}>, are you ready to play?",
            "Ready or not, <@${discordId}>, here we go!",
            "Your turn has arrived, <@${discordId}>!",
            "<@${discordId}>, it's time to play!",
            "Attention <@${discordId}>: your turn is up!",
            "It's time to make your move, <@${discordId}>.",
            "<@${discordId}>, we're counting on you to keep the game going!",
            "The game awaits, <@${discordId}>.",
            "Your move, <@${discordId}>. Make it count!",
            "We're all waiting on you, <@${discordId}>.",
            "You're up, <@${discordId}>! Let's do this.",
            "The fate of the game is in your hands, <@${discordId}>.",
            "Awaiting instructions, <@${discordId}>.",
            "I am <@${discordId}>, please insert girder",
        )

        return messages[rnd.nextInt(messages.size)]
    }

    /**
     * Retrieve existing messages in the channel, delete all but the oldest N
     */
    private fun cleanMessages() {
        val getMessagesRequest = requestBase()
            .uri(channelMessageUri)
            .build();

        val httpResponse = client.send(getMessagesRequest, HttpResponse.BodyHandlers.ofString())
        if (httpResponse.statusCode() != 200) {
            println("Error getting messages: ${httpResponse.body()}")
            throw RuntimeException("Error getting messages")
        }
        val messages: JsonNode = this.mapper.readTree(httpResponse.body())
        val idsToDelete = determineDeletions(messages, this.messagesToKeep)
        println("Deleting ${idsToDelete.size} old messages")

        idsToDelete.forEach { id ->
            val deleteRequest = requestBase()
                .uri(getDeleteUri(id))
                .DELETE()
                .build()

            val deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString())
            if (deleteResponse.statusCode() != 204) {
                println("Error deleting message ${id}: ${deleteResponse.body()}")
                throw RuntimeException("Error deleting message ${id}")
            }
        }
    }

    companion object {

        // Consume messages json from discord API, and return a list of ids to delete (keep the newest N)
        fun determineDeletions(messages: JsonNode, numberToKeep: Int): List<String> {
            val totalMessages = messages.size()
            val toDelete = max(0, totalMessages - numberToKeep)

            return messages.sortedBy { it["timestamp"].asText() }
                .take(toDelete)
                .map { it["id"].asText() }
        }
    }
}
