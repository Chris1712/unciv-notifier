package personal.chris.unciv.notifier

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.assertContains
import kotlin.test.assertEquals

class NotifierTest {

    private val mapper = ObjectMapper()

    // Messages.json has 8 messages total
    // 4 messages at 2023-03-14T19..
    // with ids 1085284636202123396 1085278788654477393 1085278288806686894 1085278090621616199
    // and 4 at 2023-03-14T20...
    // with ids 1085303165857058877 1085299988193620109 1085296690904440992 1085291619231744132

    @Test
    fun picksCorrectMessagesToDelete() {
        val messages: JsonNode = mapper.readTree(Paths.get("src/test/resources/messages.json").toFile())
        val output = Notifier.determineDeletions(messages, 4)

        assertEquals(4, output.size)
        assertContains(output, "1085284636202123396")
        assertContains(output, "1085278788654477393")
        assertContains(output, "1085278288806686894")
        assertContains(output, "1085278090621616199")
    }

    @Test
    fun canDelete0() {
        val messages: JsonNode = mapper.readTree(Paths.get("src/test/resources/messages.json").toFile())
        val output = Notifier.determineDeletions(messages, 8)

        assertEquals(0, output.size)
    }

}