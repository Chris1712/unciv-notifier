package personal.chris.unciv.notifier

import java.nio.file.*
import java.util.concurrent.atomic.AtomicInteger


class Monitor(config: Config, private val notifier: Notifier) {

    private val saveFileParentDir: Path
    private val saveFilesAbsolute: List<Path>
    private val saveFilesRelative: List<Path>
    private val watcher: WatchService
    private val queues: Map<String, AtomicInteger>

    init {
        println("Attempting to set up monitor for ${config.saveDir}")

        watcher = FileSystems.getDefault().newWatchService()
        saveFileParentDir = Paths.get(config.saveDir)
        saveFilesAbsolute = config.saveFiles.map { getAbsoluteFromRelative(it) }
        saveFilesRelative = saveFilesAbsolute.map { saveFileParentDir.relativize(it) }
        saveFileParentDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
        queues = config.saveFiles.associateBy( { it }, {AtomicInteger(0)} )
    }

    /**
     * Method to monitor the watcher for events indefinitely.
     * @see https://docs.oracle.com/javase/tutorial/essential/io/notification.html#register
     * We have to monitor the entire dir and filter to events on the files we care about
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
                else if (!saveFilesRelative.contains(event.context())) {
                    println("Event for ${event.context()} is NOT for the monitored save files, ignoring")
                    continue
                } else {
                    println("Event for ${event.context()} is for a monitored save file, continuing")
                    val saveName: String = event.context().toString()
                    Thread{handleModification(saveName)}.start() // Trigger the notification in a new thread to debounce
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
    private fun handleModification(saveName: String) {
        queues[saveName]!!.incrementAndGet()
        Thread.sleep(2000L)
        val queueSizeEnd = queues[saveName]!!.decrementAndGet()

        if (queueSizeEnd > 0) {
            return
        } else {
            println("Debounce period finished, continuing to notify")
            val nextPlayer = UncivParser.getNextTurnUuid(getAbsoluteFromRelative(saveName))
            notifier.notify(nextPlayer, saveName)
        }
    }

    private fun getAbsoluteFromRelative(fileName: String): Path {
        return saveFileParentDir.resolve(fileName)
    }

}
