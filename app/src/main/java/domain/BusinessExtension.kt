package domain

import android.util.Log
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalStdlibApi::class)
fun String.hashMD5() : String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}

fun convertTimeIntoLocalTime(time : String) : LocalDateTime? {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    try {
        return LocalDateTime.parse(time, formatter)
    } catch (e : Exception) {
        Log.e("convertTimeIntoLocalTime", e.toString())
        return null
    }
}