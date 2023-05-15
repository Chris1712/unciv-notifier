package personal.chris.unciv.notifier

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class Notifier(config: Config) {

    private val client: HttpClient
    private val rnd: Random
    private val token: String
    private val channelMessageSendUri: URI
    private val uncivToDiscordUserMap: Map<UUID, String>

    init {
        this.client = HttpClient.newBuilder().build()
        this.rnd = Random()
        this.token = config.discordToken
        this.channelMessageSendUri = URI.create("https://discord.com/api/v9/channels/${config.discordChannelId}/messages")!!
        this.uncivToDiscordUserMap = config.uncivToDiscordUserMap
    }

    fun notify(target: UUID, saveName: String) {
        println("Notifying unciv uuid $target that it is their turn in game $saveName")
        val targetDiscordId = uncivToDiscordUserMap[target]
        if (targetDiscordId == null) {
            println("No discord user mapped to unciv uuid ${target} - check config file")
            return
        } else {
            println("Mapped unciv uuid ${target} to discord user id ${targetDiscordId}")
        }


        // Make the request - if it fails, wait 2s and try again
        var retryCount = 0;
        while (retryCount < 3) {
            try {
                sendNotification(targetDiscordId, saveName)
                println("Notification sent to unciv uuid ${target}, discord user id ${targetDiscordId}")
                break
            } catch (e: Exception) {
                println("Error sending notification to unciv uuid ${target}, discord user id ${targetDiscordId} - retrying in 2s")
                Thread.sleep(2000)
            }
            retryCount++
            if (retryCount == 3) {
                println("3 failures, giving up on notifying unciv uuid ${target}, discord user id ${targetDiscordId}")
            }
        }

    }

    fun sendNotification(targetDiscordId: String, saveName: String) {
        val messageBody = """
            {
                "content": "${getMessage(targetDiscordId, saveName)}"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(channelMessageSendUri)
            .POST(HttpRequest.BodyPublishers.ofString(messageBody))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bot $token")
            .build();

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw RuntimeException("Received bad status code ${response.statusCode()}")
        }
    }

    fun getMessage(discordId: String, saveName: String): String {
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
        )

        return "Game ${saveName.substring(0,4)} : " + messages[rnd.nextInt(messages.size)]
    }
}
