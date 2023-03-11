package personal.chris.unciv.notifier

import java.io.File
import java.nio.file.*
import kotlin.system.exitProcess


class Monitor(args: Array<String>, private val notifier: Notifier) {

    private val saveFileAbsolute: Path
    private val saveFileParentDir: Path
    private val saveFileRelative: Path
    private val watcher: WatchService

    init {
        val validationError = validateArgs(args)
        if (validationError != null) {
            println(validationError)
            exitProcess(1)
        }
        println("Attempting to set up monitor for ${args[0]}")

        watcher = FileSystems.getDefault().newWatchService()
        saveFileAbsolute = Paths.get(args[0]);
        saveFileParentDir = saveFileAbsolute.parent
        saveFileRelative = saveFileParentDir.relativize(saveFileAbsolute)
        println("Located save file at ${saveFileAbsolute} with parent dir ${saveFileParentDir}")
        saveFileParentDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
    }

    /**
     * Method to monitor the watcher for events indefinitely.
     * @see https://docs.oracle.com/javase/tutorial/essential/io/notification.html#register
     */
    fun monitor() {
        while(true) {
            println("Waiting for event...")
            val key = watcher.take() // Blocks until an event is available
            println("Events received!")

            // Cycle through all events
            for (event in key.pollEvents()) {
                println("Event: ${event.kind()}")
                if (event.kind() != java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY) {
                    println("Other event: ${event.kind()}")
                    continue
                }
                else if (event.context() != saveFileRelative) {
                    println("Event for ${event.context()} is not the save file, ignoring")
                    continue
                } else {
                    handleModification()
                }
            }

            // Reset the key before we go back to waiting.
            val valid = key.reset()
            if (!valid) {
                println("Key is no longer valid, exiting")
                break
            }
        }
    }

    private fun handleModification() {
        println("Save file modified!")
        val nextPlayer = UncivParser.getNextTurn(saveFileAbsolute)
        // TODO don't notify the same person 2x in a row, in case of repeated modification
        notifier.notify(nextPlayer);
    }

    companion object {

        fun validateArgs(args: Array<String>): String? {
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