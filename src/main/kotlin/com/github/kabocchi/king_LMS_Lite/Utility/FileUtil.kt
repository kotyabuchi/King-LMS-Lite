package com.github.kabocchi.king_LMS_Lite.Utility

import java.io.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


fun saveFile(path: String, vararg objs: Any) {
    try {
        val fw = FileWriter(path)
        val pw = PrintWriter(BufferedWriter(fw))
        for (obj in objs) {
            pw.print(obj)
        }
        pw.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun readFile(file: File): String {
    var result = ""
    val scan = Scanner(file)
    var i = scan.nextLine()
    if (i != null) {
        result = i
    }
    return result
}

fun encryptFile(path: String, obj: Any) {
    val key = "aknsdkjbakjwbdka"
    val algorithm = "AES"
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), algorithm))
    val value = obj.toString()
    try  {
        val f = FileOutputStream(path)
        val b = BufferedOutputStream(f)
        val out= CipherOutputStream(b, cipher)
        out.write(value.toByteArray())
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun decryptFile(path: String): String? {
    val key = "aknsdkjbakjwbdka"
    val algorithm = "AES"
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), algorithm))
    val out = ByteArrayOutputStream()
    var data = ByteArray(1024)
    try {
        val fis = FileInputStream(path)
        val bis = BufferedInputStream(fis)
        val cis = CipherInputStream(bis, cipher)
        var num = cis.read(data)
        while (num != -1) {
            out.write(data, 0, num)
        }
        return String(out.toByteArray())
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

fun encryptFile2(path: String, obj: Any) {
    val key = SecretKeySpec("dkanwkdlnalsndia".toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val fos = FileOutputStream(path)
    val cos = CipherOutputStream(fos, cipher)
    fos.write(cipher.getIV())
    cos.write(obj.toString().toByteArray())
    cos.flush()
    cos.close()
}

fun decryptFile2(path: String): String {
    val fis = FileInputStream(path)
    val key = SecretKeySpec("dkanwkdlnalsndia".toByteArray(), "AES")
    val iv = ByteArray(16)
    fis.read(iv)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val ivspec = IvParameterSpec(iv)
    cipher.init(Cipher.DECRYPT_MODE, key, ivspec)
    val cis = CipherInputStream(fis, cipher)
    val reader = cis.bufferedReader()

    val builder = StringBuilder()
    var line: String? = reader.readLine()
    while (line != null) {
        builder.append(line)
        line = reader.readLine()
    }
    reader.close()
    return builder.toString()
}
