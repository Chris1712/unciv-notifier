package personal.chris.unciv.notifier

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.util.*

data class Config(val savePath: String, val discordToken: String, val discordChannelId: String, val uncivToDiscordUserMap: Map<UUID, String>) {

    companion object {

        fun fromArgs(args: Array<String>): Config {

            val validationError = validateArgs(args)
            if (validationError != null) {
                throw IllegalArgumentException(validationError)
            }

            val configFile = args[0]
            val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

            val config = mapper.readValue(File(configFile), Config::class.java)
            println("Loaded config from ${configFile}:\n${config}")
            return config
        }

        fun validateArgs(args: Array<String>): String? {
            if (args.size != 1) {
                return "Must supply 1 arg (path to config.yaml), received: ${args.size}"
            }

            val filePath = args[0]
            return if (!File(filePath).exists()) {
                "File at ${filePath} does not exist"
            } else {
                null
            }
        }
    }

}
