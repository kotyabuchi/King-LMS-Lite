package com.github.kabocchi.king_LMS_Lite.Utility

import com.eclipsesource.json.Json
import com.github.kabocchi.king_LMS_Lite.*
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.TextFlow
import org.apache.http.HttpStatus
import org.apache.http.client.CookieStore
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.Desktop
import java.io.IOException
import java.net.URI

fun loginToKINGLMS(_id: String = "", _pass: String = "", loadFromFile: Boolean = false): LoginResult {
    val start = System.currentTimeMillis()

    var id = _id
    var pass = _pass
    if (loadFromFile) {
        if (!PROJECT_FOLDER.exists()) PROJECT_FOLDER.mkdirs()
        if (ACCOUNT_FILE.exists()) {
            val json = Json.parse(decryptFile2(ACCOUNT_FILE_PATH)).asObject()
            id = json.getString("id", "")
            pass = json.getString("pass", "")
        }
    }
    try {
        HttpClientBuilder.create().setRedirectStrategy(LaxRedirectStrategy()).build().use { httpClient ->
            val config = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build()

            val formParams = mutableListOf<BasicNameValuePair>()
            formParams.add(BasicNameValuePair("__VIEWSTATE", "/wEPDwULLTE2MDkwMzkxOTRkZHY/AzvXjoMqTsVgJd4ipDEPUaNz"))
            formParams.add(BasicNameValuePair("__VIEWSTATEGENERATOR", "C57CFBF9"))
            formParams.add(BasicNameValuePair("__EVENTVALIDATION", "/wEdAASsWh7OxHZiOdC3v4rgI+lhmoSnNhet8R/Uqc0Y+L4tIt5lw99SYJ+Wv9EE4DvTk2BF8gstbfJCPOTeBk01E6UD2dD7i/ZD0yK6ahPUIO4y8Y1lnjI="))
            formParams.add(BasicNameValuePair("TextLoginID", id))
            formParams.add(BasicNameValuePair("TextPassword", pass))
            formParams.add(BasicNameValuePair("buttonHtmlLogon", "ログイン"))
            val entity = UrlEncodedFormEntity(formParams, "UTF-8")

            val httpPost = HttpPost("https://king.kcg.kyoto/campus/Secure/login.aspx")

            httpPost.addHeader("User-Agent", "Mozilla/5.0")
            httpPost.config = config
            httpPost.entity = entity

            httpClient.execute(httpPost, context).use { httpResponse ->
                val end = System.currentTimeMillis()
                println("Login : " + (end - start).toString() + "ms")
                return if (httpResponse.statusLine.statusCode == HttpStatus.SC_OK) {
                    if (EntityUtils.toString(httpResponse.entity).contains("span id=\"lblWarning\" class=\"error-message")) {
                        LoginResult.FAIL
                    } else {
                        LoginResult.SUCCESS
                    }
                } else {
                    LoginResult.FAIL
                }
            }
        }
    } catch (e: Exception) {
        throw e
//                if (useSchoolWifi() && wifiLogin(id, pass)) {
//                    TimeUnit.SECONDS.sleep(3)
//                    loginCount++
//                } else {
//                    break
//                }
    }
}

fun getDocumentWithJsoup(cookieMap: Map<String, String>, url: String): Document? {
    return try {
        Jsoup.connect(url).cookies(cookieMap).ignoreContentType(true).timeout(0).get()
    } catch (e: IOException) {
        throw e
    }
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
            .replace("\n", "")
    return result.trim()
}

fun cleanDescriptionVer2(_text: String): VBox {
    val textFlow = TextFlow().apply {
        maxWidth = 1200.0
        prefWidth = 1200.0
    }
    val result = VBox(4.0, textFlow)

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
    return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setRedirectStrategy(LaxRedirectStrategy()).setConnectionManager(PoolingHttpClientConnectionManager()).build()
}

fun CookieStore.toMap(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    this.cookies.forEach {
        result[it.name] = it.value
    }
    return result
}
