package util

import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile

class FileManager {
    private val logger = KotlinLogging.logger {}

    fun unzipFile(file: File): String {
        logger.debug("Unzipping file: " + file.absolutePath);
        try {
            ZipFile(file).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        File(file.absolutePath + entry.name).outputStream().use { output ->
                            input.copyTo(output)
                            logger.debug("Unzipped file to: " + file.absolutePath + entry.name);
                            return file.absolutePath + entry.name
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            logger.error("Exception unzipping file: " + file.absolutePath)
            throw exception
        }
        return ""
    }

    fun deleteFile(file: File): Boolean {
        logger.debug("Deleting file: " + file.absolutePath);
        try {
            return file.delete()
        } catch (exception: Exception) {
            logger.error("Exception deleting to file " + file.absolutePath)
            throw exception
        }
    }

    fun readFile(filePath: String): MutableList<String> {
        logger.debug("Reading file: " + filePath);
        try {
            return File(filePath).readLines().toMutableList()
        } catch (exception: Exception) {
            logger.error("Exception reading file " + filePath)
            throw exception
        }
    }

    fun writeFile(filePath: String, lines: List<String>) {
        logger.debug("Writing file: " + filePath);
        try {
            File(filePath).printWriter().use { out ->
                lines.forEach { line ->
                    out.println(line)
                }
            }
        } catch (exception: Exception) {
            logger.error("Exception writing to file " + filePath)
            throw exception
        }
    }
}