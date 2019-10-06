package com.krikun.mymvvm_arch.utils

import android.net.Uri
import com.krikun.mymvvm_arch.CommonApp
import android.webkit.MimeTypeMap
import java.io.*
import java.util.*

fun getFileFromUri(uri: Uri): File? {
    val mContext = CommonApp.instance
    var res: File? = null
    try {
        val outputDir = mContext.cacheDir // context being the Activity pointer

        val type = "." + MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        res = File.createTempFile(Date().toString(), type, outputDir)
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = mContext.contentResolver.openInputStream(uri)
            os = FileOutputStream(res!!)
            val buffer = ByteArray(4 * 1024)
            var length: Int
            while (true) {
                length = `is`!!.read(buffer)
                if (length > 0) {
                    os.write(buffer, 0, length)
                } else {
                    break
                }
            }
        } catch (e: IOException) {
            e.print()
        } finally {
            `is`?.close()
            os?.close()
        }
    } catch (e: Exception) {
        e.print()
    }

    return res
}

fun Uri.toFile() = try {
    File(this.path)
} catch (e: IOException) {
    null
}

fun String?.toUri(): Uri = try {
    Uri.parse(this)
} catch (e: Exception) {
    Uri.EMPTY
}
