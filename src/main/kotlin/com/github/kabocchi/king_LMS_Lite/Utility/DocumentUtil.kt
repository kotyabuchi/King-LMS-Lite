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

fun cleanDescriptionVer2(_text: String): TextFlow {
    val textFlow = TextFlow()
    textFlow.maxWidth = 1200.0
    textFlow.prefWidth = 1200.0
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
