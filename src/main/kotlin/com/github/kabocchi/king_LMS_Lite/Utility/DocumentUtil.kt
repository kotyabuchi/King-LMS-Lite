package com.github.kabocchi.king_LMS_Lite.Utility

import com.github.kabocchi.king_LMS_Lite.os
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.text.TextFlow
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit


fun login(con: Connection, id: String, pass: String): String {
    var loginCount = 0
    while (loginCount <= 1) {
        try {
            val doc = con.data("__VIEWSTATE", "/wEPDwULLTE2MDkwMzkxOTRkZHY/AzvXjoMqTsVgJd4ipDEPUaNz")
                .data("__VIEWSTATEGENERATOR", "C57CFBF9")
                .data("__EVENTVALIDATION", "/wEdAASsWh7OxHZiOdC3v4rgI+lhmoSnNhet8R/Uqc0Y+L4tIt5lw99SYJ+Wv9EE4DvTk2BF8gstbfJCPOTeBk01E6UD2dD7i/ZD0yK6ahPUIO4y8Y1lnjI=")
                .data("TextLoginID", id)
                .data("TextPassword", pass)
                .data("buttonHtmlLogon", "ログイン")
                .userAgent("Mozilla")
                .post()
            return if (doc != null && !doc.select("span#lblWarning").hasText()) {
                "success"
            } else {
                "fail"
            }
        } catch (e: Exception) {
            if (useSchoolWifi() && wifiLogin(id, pass)) {
                TimeUnit.SECONDS.sleep(3)
                loginCount++
            } else {
                break
            }
        }
    }
    return "error"
}

fun doPost(connection: Connection, url: String, datas: Map<String, String>): Document? {
    return try {
        Jsoup.connect(url).cookies(connection.response().cookies()).data(datas).userAgent("Mozilla").post()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun getDocument(connection: Connection, url: String): Document? {
    return try {
        Jsoup.connect(url).cookies(connection.response().cookies()).ignoreContentType(true).timeout(0).get()
    } catch (e: IOException) {
        throw e
    }
}

fun useSchoolWifi(): Boolean {
    return false
//    return ResolverConfiguration.open().searchlist().contains("academic.kcg.edu")
}

fun wifiLogin(id: String, pass: String): Boolean {
    println("KCGのWifiにログインしています")
    val doc = Jsoup.connect("http://www.furunosystems.co.jp/cgi2xml.cgi")
        .data("ACERA700", "OIWH8HV099I3UJBV98IJHZDLK209861KDLKIHIOIHGOIWHAIINBKLIUHGD8O9064KJ23KILSLMLXZAKWJQODJJWBLVI9JBKTUJUGAQDLK8I20JJ1KJUD9IO84LK98V9L29H98HGFVLIUH43O89HVLKUISHLK8IHG")
        .data("MSHTMLID", "MS1006")
        .data("ID", id)
        .data("PWD", pass)
        .userAgent("Mozilla")
        .post()
    return (doc.text().split(" ")[0] == "成功")
}

//fun loginWithHttpURLConnection(strUrl: String) {
//    var con: HttpsURLConnection? = null
//    val result = StringBuffer()
//
//    try {
//        val url = URL(strUrl)
//        con = url.openConnection() as HttpsURLConnection
//        con.doOutput = true
//        con.requestMethod = "POST"
//        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
//
//        val param = StringBuilder()
//        val urlEncode = URLCodec("UTF-8")
//        val encode = "UTF-8"
//        param.append("__VIEWSTATE=").append(urlEncode.encode("/wEPDwULLTE2MDkwMzkxOTRkZHY/AzvXjoMqTsVgJd4ipDEPUaNz", encode)).append("&")
//        param.append("__VIEWSTATEGENERATOR=").append(urlEncode.encode("C57CFBF9", encode)).append("&")
//        param.append("__EVENTVALIDATION=").append(urlEncode.encode("/wEdAASsWh7OxHZiOdC3v4rgI+lhmoSnNhet8R/Uqc0Y+L4tIt5lw99SYJ+Wv9EE4DvTk2BF8gstbfJCPOTeBk01E6UD2dD7i/ZD0yK6ahPUIO4y8Y1lnjI=", encode)).append("&")
//        param.append("TextLoginID=").append(urlEncode.encode("st011602", encode)).append("&")
//        param.append("TextPassword=").append(urlEncode.encode("setuna4021", encode)).append("&")
//        param.append("buttonHtmlLogon=").append(urlEncode.encode("ログイン", encode))
//        println(param.toString())
//        val writer = OutputStreamWriter(con.outputStream)
//        writer.write(param.toString())
//        writer.close()
//        con.connect()
//
//        val status = con.responseCode
//        if (status == HttpURLConnection.HTTP_OK) {
//            val ins = con.inputStream
//            var encoding = con.contentEncoding
//            if (encoding == null) {
//                encoding = "UTF-8"
//            }
//            val inReader = InputStreamReader(ins, encoding)
//            val bufReader = BufferedReader(inReader)
//            var line: String? = bufReader.readLine()
//            // 1行ずつテキストを読み込む
//            while (line != null) {
//                result.append(line)
//                line = bufReader.readLine()
//            }
//            bufReader.close()
//            inReader.close()
//            ins.close()
//        } else {
//            println(status)
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    } finally {
//        con?.disconnect()
//    }
//    println("result=\n$result")
//}

fun cleanDescription(text: String): String {
    var result = text.replace("\n", "")
            .replace("<br />", "\n")
            .replace("<br>\n <br>", "")
            .replace("<br>", "\n")
            .replace("<p>", "\n")
            .replace("\n ", "\n")
            .replace("\t", "")
    if (result.contains("<") && result.contains(">")) {
        for ((index, search)in result.split("<").withIndex()) {
            if (index != 0) {
                result = result.replace("<${search.split(">")[0]}>", "")
            }
        }
    }
    result = result.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "\'")
            .replace("&nbsp;", "")
    return result.trim()
}

fun cleanDescriptionVer2(_text: String): TextFlow {
    val textFlow = TextFlow()
    textFlow.maxWidth = 1200.0
    textFlow.prefWidth = 1200.0
    var text = _text.replace("\n", "")
            .replace("<br />", "\n")
            .replace("<br>\n <br>", "")
            .replace("<br>", "\n")
            .replace("<p>", "\n")
            .replace("\n ", "\n")
            .replace("\t", "")

    if (text.contains("<a href=\"\\&quot;")) {
        for ((index, _search) in text.split("<a href=\"\\&quot;").withIndex()) {
            if (index == 0) {
                var search = _search
                if (search.contains("<") && search.contains(">")) search = removeTag(search)
                val label = Label(replaceEscapeTag(search))
                label.maxWidth = 1200.0
                label.prefWidth = 1200.0
                textFlow.children.add(label)
            } else {
                val splits = _search.split("\\&quot;\" ")
                val url = splits[0]
                val hyperlink = Hyperlink(url)
                hyperlink.setOnAction {
                    if (os.startsWith("mac")) {
                        Runtime.getRuntime().exec(arrayOf("osascript", "-e", "open location \"" + url + "\""))
                    } else if (os.startsWith("windows")) {
                        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
                    } else if (os.startsWith("linux")) {
                        Runtime.getRuntime().exec("xdg-open $url")
                    }
                }
                textFlow.children.add(hyperlink)
                if (splits[1].split("</a>").size >= 2) {
                    var labelText = splits[1].split("</a>")[1]
                    if (labelText.contains("<") && labelText.contains(">")) labelText = removeTag(labelText)
                    val label = Label(replaceEscapeTag(labelText))
                    label.maxWidth = 1200.0
                    label.prefWidth = 1200.0
                    textFlow.children.add(label)
                }
            }
        }
    } else {
        if (text.contains("<") && text.contains(">")) text = removeTag(text)
        textFlow.children.add(Label(replaceEscapeTag(text)))
    }
    return textFlow
}

fun removeTag(text: String): String {
    var result = text
    for ((index, search)in result.split("<").withIndex()) {
        if (index != 0) {
            result = result.replace("<${search.split(">")[0]}>", "")
        }
    }
    return result
}

fun replaceEscapeTag(text: String): String {
    return text.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "\'")
            .replace("&nbsp;", "")
}
