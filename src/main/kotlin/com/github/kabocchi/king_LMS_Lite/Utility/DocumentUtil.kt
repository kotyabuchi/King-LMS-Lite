package com.github.kabocchi.king_LMS_Lite.Utility

import com.github.kabocchi.king_LMS_Lite.context
import com.github.kabocchi.king_LMS_Lite.os
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.TextFlow
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.client.CookieStore
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.Desktop
import java.io.IOException
import java.net.URI

fun doGet(url: String): HttpEntity {
    HttpClientBuilder.create().setRedirectStrategy(LaxRedirectStrategy()).build().use { httpClient ->
        val config = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000)
                .build()
        val httpGet = HttpGet(url).apply {
            addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
            setConfig(config)
        }
        try {
            httpClient.execute(httpGet, context).use {
                return it.entity
            }
        } catch (exception: Exception) {
            throw exception
        }
    }
}

fun getDocumentWithJsoup(cookieMap: Map<String, String>, url: String): Document? {
    return try {
        Jsoup.connect(url).cookies(cookieMap).ignoreContentType(true).timeout(0).get()
    } catch (e: IOException) {
        throw e
    }
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

fun cleanDescription(text: String): String {
    var result = text
    
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

fun cleanDescriptionVer2(_text: String): VBox {
    val textFlow = TextFlow().apply {
        maxWidth = 1200.0
        prefWidth = 1200.0
    }
    val result = VBox(textFlow).apply {
        spacing = 4.0
    }

    var text = _text.trim().replace("\n", "")
            .replace("<br />", "\n")
            .replace("<br>\n <br>", "")
            .replace("\\n", "")
            .replace("<br>", "\n")
            .replace("</p>", "\n")
            .replace("<div>", "\n")
            .replace("</div>", "")
            .replace("\n ", "\n")
            .replace("\n　", "\n")
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
    return result
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

fun createHttpClient(): CloseableHttpClient {
    return HttpClientBuilder.create().setRedirectStrategy(LaxRedirectStrategy()).build()
}

fun newLoginTest() {
    try {
        val cookieStore = BasicCookieStore()
        val context = HttpClientContext.create()
        context.cookieStore = cookieStore
        HttpClientBuilder.create().setRedirectStrategy(LaxRedirectStrategy()).build().use { httpClient ->
            val config = RequestConfig.custom()
                    .setSocketTimeout(3000)
                    .setConnectTimeout(3000)
                    .build()

            val formparams = mutableListOf<BasicNameValuePair>()
            formparams.add(BasicNameValuePair("__VIEWSTATE", "/wEPDwULLTE2MDkwMzkxOTRkZHY/AzvXjoMqTsVgJd4ipDEPUaNz"))
            formparams.add(BasicNameValuePair("__VIEWSTATEGENERATOR", "C57CFBF9"))
            formparams.add(BasicNameValuePair("__EVENTVALIDATION", "/wEdAASsWh7OxHZiOdC3v4rgI+lhmoSnNhet8R/Uqc0Y+L4tIt5lw99SYJ+Wv9EE4DvTk2BF8gstbfJCPOTeBk01E6UD2dD7i/ZD0yK6ahPUIO4y8Y1lnjI="))
            formparams.add(BasicNameValuePair("TextLoginID", "st011602"))
            formparams.add(BasicNameValuePair("TextPassword", "setuna4021"))
            formparams.add(BasicNameValuePair("buttonHtmlLogon", "ログイン"))
            val entity = UrlEncodedFormEntity(formparams, "UTF-8")
            
            val httpPost = HttpPost("https://king.kcg.kyoto/campus/Secure/login.aspx")
            
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
            httpPost.config = config
            httpPost.entity = entity

            try {
                httpClient.execute(httpPost, context).use { httpResponse ->
                    if (httpResponse.statusLine.statusCode == HttpStatus.SC_OK) {
                        println("Login Success")
                    } else {
                        println("200以外のステータスコードが返却されました。")
                    }
                }
            } catch (exception: Exception) {
                throw exception
            }
        }
        
//        HttpClientBuilder.create().setRedirectStrategy(LaxRedirectStrategy()).build().use { httpClient ->
//            val config = RequestConfig.custom()
//                    .setSocketTimeout(3000)
//                    .setConnectTimeout(3000)
//                    .build()
//            val httpGet = HttpGet("https://king.kcg.kyoto/campus/Download/DownloadHandler.aspx?q=f1kAAA8aQBAAxOH2Awdj1DIpW3ZUWgSkA5XzKdBg&f=2019%E7%A7%8B%E5%AD%A6%E6%9C%9FTOEICIP%E3%83%86%E3%82%B9%E3%83%88.pdf").apply {
//                addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
//                setConfig(config)
//            }
//            try {
//                httpClient.execute(httpGet, context).use {
//                    val inputStream = it.entity.content
//                    val filePath = "2019秋学期TOEICIPテスト.pdf"
//                    val fileOutputStream = FileOutputStream(File(filePath))
//
//                    var inByte = inputStream.read()
//                    while (inByte != -1) {
//                        fileOutputStream.write(inByte)
//                        inByte = inputStream.read()
//                    }
//                    inputStream.close()
//                    fileOutputStream.close()
//                }
//            } catch (exception: Exception) {
//                throw exception
//            }
//        }
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
}

fun CookieStore.toMap(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    this.cookies.forEach {
        result[it.name] = it.value
    }
    return result
}
