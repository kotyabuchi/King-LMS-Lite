package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.NewsCategory
import com.github.kabocchi.king_LMS_Lite.Node.SettingPane
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescription
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescriptionVer2
import com.github.kabocchi.king_LMS_Lite.Utility.createHttpClient
import com.github.kabocchi.king_LMS_Lite.context
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.FileChooser
import javafx.util.Duration
import org.apache.http.HttpStatus
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import java.io.File
import java.io.FileOutputStream

class NewsContent(doc: String, _unread: Boolean, published: String, newsCategoryMap: MutableMap<Int, NewsCategory>): VBox() {

    private val json = Json.parse(doc).asObject()
    private var description = doc.split("\"Body\": \"")[1].split("\"SenderId\":")[0].trim().removeSuffix("\",")
    private val files = json.get("Files").asArray()

    private val tagBox: HBox
    private var unreadLabel: Label? = null

    var unread = _unread
    var emergency = false
    var important = false
    val category = newsCategoryMap[json.getInt("CategoryId", 1)]!!

    val title: String = json.getString("Title", "")

    private var simpleDescription = ""
    private var longDescription: VBox

    private var openMark: Label

    init {
        this.spacing = 4.0
        this.cursor = Cursor.HAND
        this.padding = Insets(10.0, 30.0, 10.0, 10.0)
        this.styleClass.add("content-box")

        tagBox = HBox().apply {
            styleClass.add("tag-box")
            spacing = 10.0
            padding = Insets(0.0)
            setMargin(this, Insets(-2.0, 0.0, -4.0, 0.0))

            if (unread) {
                this@NewsContent.styleClass.add("unread")
                unreadLabel = Label("未読").apply {
                    textFill = Color.web("#ff4500")
                }
                children.add(unreadLabel)
            } else {
                this@NewsContent.styleClass.add("alreadyRead")
            }

            when (json.getString("Priority", "普通")) {
                "緊急" -> {
                    emergency = true
                    val priorityLabel = Label("緊急").apply {
                        textFill = Color.web("#ff4500")
                    }
                    children.add(priorityLabel)
                }
                "重要" -> {
                    important = true
                    val priorityLabel = Label("重要").apply {
                        textFill = Color.web("#ff4500")
                    }
                    children.add(priorityLabel)
                }
            }

            val categoryLabel = Label(category.categoryName)
            children.add(categoryLabel)
        }

        val topBorderPane = BorderPane()

        val titleText = Label(title).apply {
            style = "-fx-font-weight: bold;"
        }
        topBorderPane.left = titleText

        val dateText = Label("掲載日: $published").apply {
            style = "-fx-font-size: 13px; -fx-font-weight: bold;"
        }
        topBorderPane.right = dateText

        openMark = Label("▼").apply {
            style = "-fx-font-size: 10px;"
        }

        if (description.trim().isBlank()) {
            description = "このお知らせには詳細文がありません"
            longDescription = VBox().apply {
                val descriptionLabel = Label(description).apply {
                    isWrapText = true
                    textFill = Color.GRAY
                }
                children.add(descriptionLabel)
            }
        } else {
            simpleDescription = cleanDescription(description)
            longDescription = cleanDescriptionVer2(description).apply {
                minHeight = 0.0
                var descHeight = 0.0
                var open = false
                var animating = false
                layoutBoundsProperty().addListener(ChangeListener { _, oldValue, newValue ->
                    if (descHeight == 0.0 && newValue.height > 0.0) {
                        descHeight = newValue.height
                        val openAnim = Timeline(
                                KeyFrame(Duration.seconds(0.2), KeyValue(maxHeightProperty(), descHeight)),
                                KeyFrame(Duration.seconds(0.2), KeyValue(openMark.rotateProperty(), 180.0))).apply {
                            cycleCount = 1
                            setOnFinished {
                                animating = false
                            }
                        }
                        val closeAnim = Timeline(
                                KeyFrame(Duration.seconds(0.2), KeyValue(maxHeightProperty(), 0.0)),
                                KeyFrame(Duration.seconds(0.2), KeyValue(openMark.rotateProperty(), 0.0))).apply {
                            cycleCount = 1
                            setOnFinished {
                                animating = false
                            }
                        }
                        this@NewsContent.setOnMouseClicked {
                            if (animating) return@setOnMouseClicked
                            animating = true
                            if (open) {
                                closeAnim.play()
                            } else {
                                setRead()
                                openAnim.play()
                            }
                            open = !open
                        }
                        Platform.runLater {
                            maxHeight = 0.0
                        }
                    }
                })
            }
            Rectangle().apply {
                widthProperty().bind(longDescription.widthProperty())
                heightProperty().bind(longDescription.maxHeightProperty())
                longDescription.clip = this
            }
        }

        if (files.size() > 0) {
            longDescription.children.add(0, Separator())
            this.children.addAll(tagBox, topBorderPane, longDescription, Separator(),
                    BorderPane().apply {
                        padding = Insets(0.0, 8.0, 0.0, 0.0)
                        left = Label("添付ファイル").apply {
                            style = "-fx-font-size: 12px;"
                        }
                        right = openMark
                    }
            )
            files.forEach { json ->
                json as JsonObject
                val fileName = json.getString("FileName", "")
                val hyperlink = Hyperlink(fileName).apply {
                    setOnAction {
                        val file = if (SettingPane.getNewsSaveSetting().askingEachTime != false) {
                            val chooser = FileChooser()
                            chooser.extensionFilters.add(FileChooser.ExtensionFilter("All", "*.*"))
                            chooser.initialDirectory = File(System.getProperty("user.home"))
                            chooser.initialFileName = fileName
                            chooser.showSaveDialog(null)
                        } else {
                            val saveFolder = File(SettingPane.getNewsSaveSetting().folderPath ?: "news/")
                            if (!saveFolder.exists()) saveFolder.mkdirs()
                            File(saveFolder.path + File.separator + fileName)
                        } ?: return@setOnAction
                        println("Save file($fileName) to ${file.path}")

                        createHttpClient().use { httpClient ->
                            val httpGet = HttpGet("https://king.kcg.kyoto/campus/Portal/TryAnnouncement/GetFileAttachment/${json.getInt("Id", 0)}")
                            try {
                                httpClient.execute(httpGet, context).use {
                                    val inputStream = it.entity.content
                                    val fileOutputStream = FileOutputStream(file)

                                    var inByte = inputStream.read()
                                    while (inByte != -1) {
                                        fileOutputStream.write(inByte)
                                        inByte = inputStream.read()
                                    }
                                    inputStream.close()
                                    fileOutputStream.close()
                                }
                            }catch (exception: Exception) {
                                throw exception
                            }
                        }
                    }
                }
                this.children.add(hyperlink)
            }
        } else {
            this.children.addAll(tagBox, topBorderPane, Separator(), longDescription, BorderPane().apply {
                padding = Insets(0.0, 8.0, 0.0, 0.0)
                right = openMark
            })
        }
    }

    private fun setRead() {
        if (unread) {
            createHttpClient().use { httpClient ->
                val httpPost = HttpPost("https://king.kcg.kyoto/campus/Portal/TryAnnouncement/SetReadStateAnnouncement")
                val formParams = mutableListOf<BasicNameValuePair>()
                formParams.add(BasicNameValuePair("id", json.getInt("Id", 0).toString()))
                httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                httpPost.entity = UrlEncodedFormEntity(formParams, "UTF-8")

                httpClient.execute(httpPost, context).use { httpResponse ->
                    if (httpResponse.statusLine.statusCode == HttpStatus.SC_OK) {
                        unread = false
                        this.styleClass.remove("unread")
                        this.styleClass.add("alreadyRead")
                        Platform.runLater {
                            tagBox.children.remove(unreadLabel)
                        }
                    }
                }
            }
        }
    }

    fun getDescription(): String {
        return simpleDescription
    }
}
