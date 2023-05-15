package personal.chris.unciv.notifier

import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfigTest {

    @Nested
    class FromArgs {

        @Test fun createsConfig() {
            val config = Config.fromArgs(arrayOf("src/test/resources/sample-config.yaml"))

            assertNotNull(config)
            assertEquals(2, config.saveFiles.size)
            assertEquals("4fea48b1-f8e5-4b0a-a784-23b51fec6fdc_Preview", config.saveFiles[0])
            assertEquals("19f97aa2-8a0d-4f56-9783-b448e1463074_Preview", config.saveFiles[1])
            assertEquals("abc.y8yTg565.l1I9Xfff4r", config.discordToken)
            assertEquals("1084429893288349699", config.discordChannelId)
            assertEquals(2, config.uncivToDiscordUserMap.size)
            assertEquals("78978979811112", config.uncivToDiscordUserMap[UUID.fromString("b1c460d0-bf64-4e93-ad1d-a6576aad748f")])
        }
    }

    @Nested
    class ValidateArgs {

        @Test fun validatesNoArgs() {
            val validationError = Config.validateArgs(emptyArray());

            assertNotNull(validationError)
            assertEquals("Must supply 1 arg (path to config.yaml), received: 0", validationError)
        }

        @Test fun validatesTwoArgs() {
            val validationError = Config.validateArgs(arrayOf("arg1", "arg2"));

            assertNotNull(validationError)
            assertEquals("Must supply 1 arg (path to config.yaml), received: 2", validationError)
        }

        @Test fun validatesNonExistentFile() {
            val validationError = Config.validateArgs(arrayOf("not-a-file"));

            assertNotNull(validationError)
            assertEquals("File at not-a-file does not exist", validationError)
        }
    }
}
