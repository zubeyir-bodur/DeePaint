package com.example.deepaint

import android.util.Log
import java.io.*
import java.util.zip.ZipFile

/**
 * UnzipUtils class extracts files and sub-directories of a standard zip file to
 * a destination directory.
 *
 */
object UnzipUtils {
    /**
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destDirectory: String) {

        val destDir =  File(destDirectory).run {
            if (!exists()) {
                mkdirs()
            }
        }

        ZipFile(zipFilePath).use { zip ->

            zip.entries().asSequence().forEach { entry ->

                zip.getInputStream(entry).use { input ->


                    val filePath = destDirectory + File.separator + entry.name

                    if (!entry.isDirectory) {
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        dir.mkdir()
                    }

                }

            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     * @param inputStream
     * @param destFilePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {

        var destFilePathOut : String = ""
        val length = destFilePath.length
        var dotCount = 0
        var wrong = false
        for (i in 0 until length) {
            if (destFilePath[i] == '.') {
                dotCount++
            }
            if (dotCount > 1) {
                wrong = true
                break
            }
        }
        if (dotCount != 1) {
            var lastDotIndex = destFilePath.lastIndexOf('.')
            var noExtPath = destFilePath.substring(0, lastDotIndex)
            var firstDotIndex = noExtPath.lastIndexOf('.')
            destFilePathOut = destFilePath.substring(0, firstDotIndex) + destFilePath.substring(firstDotIndex + 1, length)
            Log.d("This will be the actual fileName", destFilePathOut)
        } else {
            destFilePathOut = destFilePath
        }
        val bos = BufferedOutputStream(FileOutputStream(destFilePathOut))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    /**
     * Size of the buffer to read/write data
     */
    private const val BUFFER_SIZE = 4096

}