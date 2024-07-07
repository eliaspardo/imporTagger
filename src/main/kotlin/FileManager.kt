import mu.KotlinLogging
import java.io.BufferedReader
import java.io.File
import java.util.zip.ZipFile

class FileManager {
    private val logger = KotlinLogging.logger {}

    fun unzipFile(file: File) : String{
        logger.debug("Unzipping file: "+file.absolutePath);
        ZipFile(file).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    File(file.absolutePath+entry.name).outputStream().use { output ->
                        input.copyTo(output)
                        logger.debug("Unzipped file to: "+file.absolutePath+entry.name);
                        return file.absolutePath+entry.name
                    }
                }
            }
        }
        return ""
    }

    fun deleteFile(file:File): Boolean{
        logger.debug("Deleting file: "+file.absolutePath);
        return file.delete()
    }

    fun readFile(filePath: String): MutableList<String> {
        logger.debug("Reading file: "+filePath);
        return File(filePath).readLines().toMutableList()
    }

    fun writeFile(filePath: String, lines: List<String>) {
        logger.debug("Writing file: "+filePath);
        File(filePath).printWriter().use { out ->
            lines.forEach { line ->
                out.println(line)
            }
        }
    }
}