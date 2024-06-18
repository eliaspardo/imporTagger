import java.io.BufferedReader
import java.io.File
import java.util.zip.ZipFile

class FileManager {
    fun unzipFile(file: File) : String{
        ZipFile(file).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    File(file.absolutePath+entry.name).outputStream().use { output ->
                        input.copyTo(output)
                        return file.absolutePath+entry.name
                    }
                }
            }
        }
        return ""
    }

    fun deleteFile(file:File): Boolean{
        return file.delete()
    }
}