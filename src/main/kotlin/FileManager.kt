import java.io.File
import java.util.zip.ZipFile

class FileManager {
    // TODO This should return a list of files
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