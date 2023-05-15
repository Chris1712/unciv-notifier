package personal.chris.unciv.notifier

import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.net.http.HttpRequest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.concurrent.thread
import kotlin.test.Test
import java.nio.file.FileAlreadyExistsException as FileAlreadyExistsException1


class AppTest {

    val baseDirectory: Path = Paths.get("/tmp/unciv-saves-test/")
    val testFile1: Path = Paths.get("/tmp/unciv-saves-test/4fea48b1-f8e5-4b0a-a784-23b51fec6fdc_Preview")
    val testFile2: Path = Paths.get("/tmp/unciv-saves-test/19f97aa2-8a0d-4f56-9783-b448e1463074_Preview")

    val sampleSaveText = Files.readAllBytes(Paths.get("src/test/resources/sample2_Preview"))

    @Test
    fun e2e() {
        // Create a test directory to monitor
        Files.createDirectories(baseDirectory)
        // Create test files
        listOf(testFile1, testFile2).forEach {
            try {
                Files.createFile(it)
            } catch (e: FileAlreadyExistsException1) {
                println("File already exists")
            }
        }
        val requestCaptor = ArgumentCaptor.forClass(HttpRequest::class.java)

        val config = Config.fromArgs(arrayOf("src/test/resources/sample-config.yaml"))
        val mockNotifier = Mockito.mock<Notifier>()
        val monitor = Monitor(config, mockNotifier)

        thread {
            monitor.monitor()
        }

        // Write to the file, sleep, and see what was written out.
        Files.write(testFile1, sampleSaveText)
        Thread.sleep(2500) // 2s debounce timer

        Mockito.verify(mockNotifier).notify(
            UUID.fromString("b1c460d0-bf64-4e93-ad1d-a6576aad748f"),
            "4fea48b1-f8e5-4b0a-a784-23b51fec6fdc_Preview")
    }

}