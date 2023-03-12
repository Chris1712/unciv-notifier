package personal.chris.unciv.notifier

import java.io.File
import java.nio.file.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess


class Monitor(args: Array<String>, private val notifier: Notifier) {

    private val saveFileAbsolute: Path
    private val saveFileParentDir: Path
    private val saveFileRelative: Path
    private val watcher: WatchService
    private val queue = AtomicInteger(0)

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
     * We have to monitor the entire dir and filter to events on the file we care about
     * Note that in practise we get many modification events, some of which seem to be before the file is safe to read.
     * So we debounce the event handling.
     */
    fun monitor() {
        while(true) {
            println("Waiting for event...")
            val key = watcher.take() // Blocks until an event is available
            // Cycle through all events
            for (event in key.pollEvents()) {
                if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) {
                    println("Other event: ${event.kind()}")
                    continue
                }
                else if (event.context() != saveFileRelative) {
                    println("Event for ${event.context()} is not the save file, ignoring")
                    continue
                } else {
                    Thread{handleModification()}.start() // Trigger the notification in a new thread to debounce
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

    /**
     * To be triggered concurrently - only proceeds when no other calls for 2s
     */
    private fun handleModification() {
        println("handleModification start!")
        val queueSizeStart = queue.incrementAndGet()
        println("Queue size start: ${queueSizeStart}")
        Thread.sleep(2000L)
        val queueSizeEnd = queue.decrementAndGet()
        println("Queue size end: ${queueSizeEnd}")

        if (queueSizeEnd > 0) {
            return
        } else {
            val nextPlayer = UncivParser.getNextTurnUuid(saveFileAbsolute)
            // TODO don't notify the same person 2x in a row, in case of repeated modification
            notifier.notify(nextPlayer.toString())
        }
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