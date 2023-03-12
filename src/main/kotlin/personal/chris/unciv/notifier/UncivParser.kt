package personal.chris.unciv.notifier

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.*
import java.util.zip.GZIPInputStream

class UncivParser {

    companion object {
        fun getNextTurnUuid(saveFile: Path): UUID {
            val saveFileContents: String = extractObjectFromFile(saveFile)

            // Save file is a strange format, maybe json light? Hard to deserialize. So for now we just do some regex.

            // We have a section like "currentPlayer: France,"
            val civMatch = Regex("currentPlayer:([^,]*),").find(saveFileContents)
                ?: throw Exception("Couldn't find current civ in save file")
            val currentCiv: String = civMatch.groupValues[1]
            // 'France' isn't much use, we need to find the player UUID for this civ.
            // We have a section like "civName:France,\n      playerId:2cba587d-e619-4dc7-8b20-d31a032d58be,":
            val playerMatch = Regex("""civName:$currentCiv,[^\}]*playerId:([^,}]*)""").find(saveFileContents)
                ?: throw Exception("Couldn't find player UUID for civ $currentCiv in save file")
            val currentPlayerString: String = playerMatch.groupValues[1]

            try {
                return UUID.fromString(currentPlayerString)
            } catch (e: IllegalArgumentException) {
                throw Exception("Couldn't parse player UUID $currentPlayerString")
            }
        }

        fun extractObjectFromFile(saveFile: Path): String {
            // An unciv preview file is a base64 encoded gzip file.

            // We read the file as a string:
            val contents: String = saveFile.toFile().readText()
            println("Extracting object from file ${saveFile.fileName} - (${contents.length} chars)")
            println("First 10 chars of contents: ${contents.substring(0, 10)}")
            println("Last 10 chars of contents: ${contents.substring(contents.length - 10)}")
            //  Decode this base64 string to bytes:
            val compressedBytes = Base64.getDecoder().decode(contents)
            //  Unzip the bytes:
            val gzipIn = GZIPInputStream(ByteArrayInputStream(compressedBytes))
            val bytesOut = ByteArrayOutputStream()
            var res = 0
            val buf = ByteArray(1024)
            while (res >= 0) {
                res = gzipIn.read(buf, 0, buf.size)
                if (res > 0) {
                    bytesOut.write(buf, 0, res)
                }
            }
            // Turn the uncompressed bytes back into a string:
            return String(bytesOut.toByteArray())
        }


    }
}