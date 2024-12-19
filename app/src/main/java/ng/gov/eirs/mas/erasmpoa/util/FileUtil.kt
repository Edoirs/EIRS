package ng.gov.eirs.mas.erasmpoa.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileUtil {
    fun compressFile(file: File, newFile: File?, width: Int, height: Int): File? {
        try {
            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image

            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to


            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= width && o.outHeight / scale / 2 >= height) {
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)

            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()

            val compressedFile = newFile ?: file
            compressedFile.createNewFile()

            val outputStream = FileOutputStream(compressedFile)
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            return compressedFile
        } catch (e: Exception) {
            return null
        }
    }
}