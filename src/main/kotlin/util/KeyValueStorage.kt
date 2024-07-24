package util

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class KeyValueStorage: IKeyValueStorage {
    companion object {
        @Volatile
        private var instance: KeyValueStorage? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: KeyValueStorage().also { instance = it }
            }

    }

    private val settings: Settings by lazy { Settings() }

    override var token: String?
        get() = settings[StorageKeys.TOKEN.key]
        set(value) {
            settings[StorageKeys.TOKEN.key] = value
        }

    // clean all the stored values
    override fun cleanStorage() {
        settings.clear()
    }
}