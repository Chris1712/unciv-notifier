package personal.chris.unciv.notifier

import java.io.File
import java.nio.file.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess


class Monitor(config: Config, private val notifier: Notifier) {

    private val saveFileAbsolute: Path
    private val saveFileParentDir: Path
    private val saveFileRelative: Path
    private val watcher: WatchService
    private val queue = AtomicInteger(0)

    init {
        println("Attempting to set up monitor for ${config.savePath}")

        watcher = FileSystems.getDefault().newWatchService()
        saveFileAbsolute = Paths.get(config.savePath);
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
                    println("Event for ${event.context()} is NOT the save file, ignoring")
                    continue
                } else {
                    println("Event for ${event.context()} is the save file, continuing")
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
        queue.incrementAndGet()
        Thread.sleep(2000L)
        val queueSizeEnd = queue.decrementAndGet()

        if (queueSizeEnd > 0) {
            return
        } else {
            println("Debounce period finished, continuing to notify")
            val nextPlayer = UncivParser.getNextTurnUuid(saveFileAbsolute)
            notifier.notify(nextPlayer)
        }
    }

}
