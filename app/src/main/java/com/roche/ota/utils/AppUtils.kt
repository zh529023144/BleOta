package com.roche.ota.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Environment
import com.roche.ota.api.UrlConstant
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern

class AppUtils private constructor() {


    init {
        throw Error("Do not allow instance!")
    }

    companion object {

        val TAG = "APPUtils"

        fun getVersionName(context: Context): String {
            var versionName = ""
            try {
                val packageName = context.packageName
                versionName = context.packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: NameNotFoundException) {
                e.printStackTrace()
            }
            return versionName
        }

        private fun getVersionCode(context: Context): Int {
            val packageInfo = getPackageInfo(context)
            return packageInfo?.versionCode ?: 0
        }

        private fun getPackageInfo(context: Context): PackageInfo? {
            try {
                return context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: NameNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        fun getMobileModel(): String {
            var model: String? = Build.MODEL
            model = model?.trim { it <= ' ' } ?: ""
            return model
        }


        //判断 密码规则
        fun isMathPassWord(password: String): Boolean {
            val pattern = Pattern.compile("^(?![A-Z]+$)(?![a-z]+$)(?!\\d+$)(?![\\W_]+$)\\S{8,16}$")
            val matcher = pattern.matcher(password)
            return matcher.matches()
        }

        //md5

        fun getMd5(plainText: String): String {
            try {
                val md = MessageDigest.getInstance("MD5")
                md.update(plainText.toByteArray())
                val b = md.digest()

                var i: Int

                val buf = StringBuffer("")
                for (offset in b.indices) {
                    i = b[offset].toInt()
                    if (i < 0)
                        i += 256
                    if (i < 16)
                        buf.append("0")
                    buf.append(Integer.toHexString(i))
                }
                // 32位加密
                // String mm=new String(original)
                return buf.toString()
                //16位的加密
                // return buf.toString().substring(8, 24);
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                return ""
            }

        }

        fun getAssetsByte(context: Context, bytesFile: ByteArray): ByteArray {
            val manager = context.resources.assets
            //0105
//        val inputStream = manager.open("N600.H9I_app.bin")


//            val inputStream = manager.open("N600.H9J_app.bin") //0108/
            val inputStream = manager.open("N600V3.1_BH9K_app.bin") //0109
            val read = DataInputStream(inputStream)
            read.read(bytesFile, 0, bytesFile.size)
            read.close()
            inputStream.close()

            return bytesFile

        }

        //下载服务器存本地
        fun getUrlDownByte(fileUrl: String) {

            var conn: HttpURLConnection? = null
            try {
                conn = URL(fileUrl).openConnection() as HttpURLConnection
                //建立链接
                conn.connect()
                //打开输入流

                conn.inputStream.use { input ->
                    BufferedOutputStream(
                        FileOutputStream(
                            File(UrlConstant.LOCAL_FILE_UPDATE), false
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

        //根据文件获取byte 数组
        fun getFileByte(file: File, bytesFile: ByteArray): ByteArray {

            if (file.exists()) {
                val inputStream = file.inputStream()

                val read = DataInputStream(inputStream)
                read.read(bytesFile, 0, bytesFile.size)
                read.close()
                inputStream.close()

                return bytesFile
            }


            return bytesFile
        }


        fun getStr(str: String): String {
            if (str.length <= 2) {
                return str
            }
            return str.substring(0, 2) + ":" + getStr(str.substring(2))
        }

    }


}