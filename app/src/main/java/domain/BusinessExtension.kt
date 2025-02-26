package domain

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.hashMD5() : String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}