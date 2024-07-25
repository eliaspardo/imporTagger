package util

interface IKeyValueStorage {
    var token: String?

    fun cleanStorage()
}