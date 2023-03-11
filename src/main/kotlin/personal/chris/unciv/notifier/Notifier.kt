package personal.chris.unciv.notifier

import java.io.File
import kotlin.system.exitProcess

class Notifier {

    companion object {

        fun MonitorUncivSave(args: Array<String>) {
            val validationError = ValidateArgs(args)
            if (validationError != null) {
                println(validationError)
                exitProcess(1)
            }
            println("Monitoring ${args[0]}")


        }


        fun ValidateArgs(args: Array<String>): String? {
            if (args.size != 1) {
                return "Must supply 1 arg (path to watch), received: ${args.size}"
            }

            val saveFilePath = args[0]
            return if (!File(saveFilePath).exists()) {
                "File at ${saveFilePath} does not exist"
            } else {
                null
            }
        }
    }

}