package util

interface KeyValueStorage {
    var token: String?

    fun cleanStorage()
}