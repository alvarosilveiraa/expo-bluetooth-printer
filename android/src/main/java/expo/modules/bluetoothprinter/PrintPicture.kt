package cn.jystudio.bluetooth.escpos.command.sdk

import android.graphics.*

object PrintPicture {
    private val p0 = intArrayOf(0, 128)
    private val p1 = intArrayOf(0, 64)
    private val p2 = intArrayOf(0, 32)
    private val p3 = intArrayOf(0, 16)
    private val p4 = intArrayOf(0, 8)
    private val p5 = intArrayOf(0, 4)
    private val p6 = intArrayOf(0, 2)
    private val Floyd16x16 = arrayOf(
        intArrayOf(0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 170),
        intArrayOf(192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106),
        intArrayOf(48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154),
        intArrayOf(240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90),
        intArrayOf(12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166),
        intArrayOf(204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102),
        intArrayOf(60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150),
        intArrayOf(252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 118, 214, 86),
        intArrayOf(3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169),
        intArrayOf(195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105),
        intArrayOf(51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153),
        intArrayOf(243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89),
        intArrayOf(15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165),
        intArrayOf(207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101),
        intArrayOf(63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149),
        intArrayOf(254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 117, 213, 85)
    )

    fun resizeImage(bitmap: Bitmap, w: Int, h: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scaleWidth = w.toFloat() / width.toFloat()
        val scaleHeight = h.toFloat() / height.toFloat()
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    fun pad(src: Bitmap, paddingX: Int, paddingY: Int): Bitmap {
        val outputImage = Bitmap.createBitmap(src.width + paddingX, src.height + paddingY, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputImage)
        canvas.drawARGB(255, 255, 255, 255) // White color
        canvas.drawBitmap(src, paddingX.toFloat(), paddingY.toFloat(), null)
        return outputImage
    }

    /**
     * Prints a bitmap
     * This function prints one line at a time, reducing errors.
     */
    fun POS_PrintBMP(mBitmap: Bitmap, nWidth: Int, nMode: Int, leftPadding: Int): ByteArray {
        // Convert to black and white, then scale the bitmap
        var width = ((nWidth + 7) / 8) * 8
        var height = mBitmap.height * width / mBitmap.width
        height = ((height + 7) / 8) * 8
        val left = if (leftPadding == 0) 0 else ((leftPadding + 7) / 8) * 8

        var rszBitmap = mBitmap
        if (mBitmap.width != width) {
            rszBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, true)
        }

        var grayBitmap = toGrayscale(rszBitmap)
        if (left > 0) {
            grayBitmap = pad(grayBitmap, left, 0)
        }

        val dithered = thresholdToBWPic(grayBitmap)
        return eachLinePixToCmd(dithered, width + left, nMode)
    }

    fun Print_1D2A(bmp: Bitmap): ByteArray {
        val width = bmp.width
        val height = bmp.height
        val data = ByteArray(1024 * 10)
        data[0] = 0x1D
        data[1] = 0x2A
        data[2] = ((width - 1) / 8 + 1).toByte()
        data[3] = ((height - 1) / 8 + 1).toByte()
        var k: Int
        var position = 4
        var temp: Byte
        for (i in 0 until width) {
            var currentByte = 0.toByte()
            var k = 0
            for (j in 0 until height) {
                if (bmp.getPixel(i, j) != -1) {
                    currentByte = (currentByte.toInt() or (0x80 shr k)).toByte()
                }
                k++
                if (k == 8) {
                    data[position++] = currentByte
                    currentByte = 0
                    k = 0
                }
            }
        }
        return data
    }

    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width = bmpOriginal.width
        val height = bmpOriginal.height
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        canvas.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    private fun thresholdToBWPic(mBitmap: Bitmap): ByteArray {
        val pixels = IntArray(mBitmap.width * mBitmap.height)
        val data = ByteArray(mBitmap.width * mBitmap.height)
        mBitmap.getPixels(pixels, 0, mBitmap.width, 0, 0, mBitmap.width, mBitmap.height)
        format_K_threshold(pixels, mBitmap.width, mBitmap.height, data)
        return data
    }

    private fun format_K_threshold(orgPixels: IntArray, xsize: Int, ysize: Int, desPixels: ByteArray) {
        var grayTotal = 0
        var k = 0
        for (i in 0 until ysize) {
            for (j in 0 until xsize) {
                val gray = orgPixels[k] and 255
                grayTotal += gray
                k++
            }
        }

        val grayAvg = grayTotal / (ysize * xsize)
        k = 0

        for (i in 0 until ysize) {
            for (j in 0 until xsize) {
                val gray = orgPixels[k] and 255
                desPixels[k] = if (gray > grayAvg) 0 else 1
                k++
            }
        }
    }

    fun eachLinePixToCmd(bmpPixels: ByteArray, width: Int, mode: Int): ByteArray {
        val result = ByteArray(width / 8 + 3)
        result[0] = 0x1D
        result[1] = 0x2A
        result[2] = ((width - 1) / 8 + 1).toByte()

        for (i in 0 until bmpPixels.size) {
            result[i + 3] = bmpPixels[i]
        }
        return result
    }
}
