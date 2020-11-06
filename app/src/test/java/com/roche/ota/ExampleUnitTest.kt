package com.roche.ota

import android.os.Environment
import android.util.Log
import com.roche.ota.utils.HexUtil
import org.junit.Test

import org.junit.Assert.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun isContains() {
        val list = arrayListOf("N600C,N800")
        val a = "N600"
        if (list.contains(a)) {
            print("存在")
        } else {
            print("不存在")
        }
    }


    @Test
    fun toHex() {
        val a = byteArrayOf(
            2,
            1,
            6,
            3,
            2,
            -32,
            -2,
            13,
            9,
            56,
            50,
            48,
            48,
            48,
            48,
            48,
            48,
            48,
            49,
            51,
            52,
            9,
            -1,
            0,
            0,
            -126,
            0,
            0,
            0,
            1,
            52,
            13,
            9,
            56,
            50,
            48,
            48,
            48,
            48,
            48,
            48,
            48,
            49,
            51,
            52,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
        )
        println("Test  转换16进制文本：" + HexUtil.encodeHexStr(a))
        val b = byteArrayOf(
            2,
            1,
            6,
            3,
            2,
            -32,
            -2,
            13,
            9,
            56,
            50,
            48,
            48,
            48,
            48,
            48,
            48,
            48,
            49,
            51,
            52,
            9,
            -1,
            0,
            0,
            -126,
            0,
            0,
            0,
            1,
            52
        )
        println("Test  转换16进制文本：广播数据：" + HexUtil.encodeHexStr(b))
        val c = byteArrayOf(
            13,
            9,
            56,
            50,
            48,
            48,
            48,
            48,
            48,
            48,
            48,
            49,
            51,
            52,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
        )
        println("Test  转换16进制文本：响应数据：" + HexUtil.encodeHexStr(c))

        val d = "0108"
        println("Test  转换byte数组：" + Arrays.toString(HexUtil.decodeHex(d)))

    }

    @Test
    fun toByte() {
        val fileUrl =
            "https://test.nofetel.com/vend/images/2020/11/02/4a182da1-8b09-4e36-a23f-f695e07e9252.1_BH9K_app.bin"

        var conn: HttpURLConnection? = null
        try {
            conn = URL(fileUrl).openConnection() as HttpURLConnection
            //建立链接
            conn.connect()
            //打开输入流

            conn.inputStream.use { input ->
                BufferedOutputStream(
                    FileOutputStream(
                        File(
                            Environment.getExternalStorageDirectory(),
                            "download.bin"
                        ), false
                    )
                ).use { output ->

                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

            conn?.disconnect()
        }

    }
}
