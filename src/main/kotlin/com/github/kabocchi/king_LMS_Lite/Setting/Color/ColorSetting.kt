package com.github.kabocchi.king_LMS_Lite.Setting.Color

import com.fasterxml.jackson.annotation.*
import com.github.kabocchi.king_LMS_Lite.FOLDER_PATH
import com.github.kabocchi.king_LMS_Lite.Utility.saveFile
import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("templates", "choseColor")
class ColorSetting {

    @get:JsonProperty("templates")
    @set:JsonProperty("templates")
    @JsonProperty("templates")
    var templates: MutableList<TemplateColor>? = null

    @get:JsonProperty("choseColor")
    @set:JsonProperty("choseColor")
    @JsonProperty("choseColor")
    var choseColor: Int = 0

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = HashMap()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    fun getColor(): TemplateColor {
        return templates?.get(choseColor) ?: templates?.get(0)!!
    }

    fun createCSS() {
        println("=====================================================")
        println(choseColor)
        println("=====================================================")
        val color = getColor()
        var cssStr = ""
        getReadAllLinesInJarFile(getURI(javaClass.getResource("/css/main_template.css").toExternalForm()))?.forEach {
            cssStr += it
        }
        cssStr = cssStr.replace("main_color", color.mainColor ?: "#F89174")
                .replace("sub_color", color.subColor ?: "white")
                .replace("sub_text_color", color.subTextColor ?: "white")
                .replace("link_text_color", color.linkColor ?: "#e74c3c")
                .replace("text_color", color.textColor ?: "rgb(30, 30, 30)")
        saveFile(FOLDER_PATH + "style.css", cssStr)
    }

    private fun getURI(src: String): URI {
        return URI.create(URL(src).toString().replace(File.separator, "/"))
    }

    @Throws(IOException::class)
    private fun getReadAllLinesInJarFile(uri: URI): List<String?>? {
        val env: MutableMap<String, String> = HashMap()
        env["create"] = "true"
        FileSystems.newFileSystem(URI.create(uri.toString().split("!").get(0)), env).use({ Jarfs -> return Files.readAllLines(Jarfs.getPath(uri.toString().split("!").get(1))) })
    }
}
