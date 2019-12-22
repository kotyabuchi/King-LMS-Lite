package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.NewsCategory
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
import javafx.scene.control.OverrunStyle
import javafx.scene.control.Separator
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.util.Duration
import org.apache.http.client.methods.HttpGet
import java.io.File
import java.io.FileOutputStream

class NewsContent(doc: String, _unread: Boolean, published: String, newsCategoryMap: MutableMap<Int, NewsCategory>): VBox() {

    private val json = Json.parse(doc).asObject()
    private var description = doc.split("\"Body\": \"")[1].split("\"SenderId\":")[0].trim().removeSuffix("\",")
    private val files = json.get("Files").asArray()

    private val tagBox: HBox
    private var unreadLabel: Label? = null
    private val separator = Separator()

    private var showingDescription = false

    var unread = _unread
    var emergency = false
    var important = false
    val category = newsCategoryMap[json.getInt("CategoryId", 1)]!!

    val title: String = json.getString("Title", "")

    private val simpleDescription: String
    private var longDescription: VBox
    private var shortDescription: Label

    private var shortHeight = 0.0
    private var longHeight = 0.0

    private var shortAnimation: Timeline? = null
    private var longAnimation: Timeline? = null

    private var animating = true

    init {
        this.spacing = 4.0
        this.cursor = Cursor.HAND
        this.padding = Insets(10.0, 30.0, 10.0, 30.0)
        this.styleClass.add("news-content-box")

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

        separator.prefWidth = this.prefWidth / 40

        val titleText = Text(title).apply {
            style = "-fx-font-size: 14px; -fx-font-weight: bold;"
        }
        topBorderPane.left = titleText

        val dateText = Text("掲載日: $published").apply {
            style = "-fx-font-size: 13px; -fx-font-weight: bold;"
        }
        topBorderPane.right = dateText

        if (description.trim().isBlank()) {
            description = "このタスクには詳細文が設定されていません"
            simpleDescription = ""
            shortDescription = Label(description).apply {
                isWrapText = false
                ellipsisString = "..."
                textOverrun = OverrunStyle.ELLIPSIS
                textFill = Color.GRAY
                if (files.size() > 0) {
                    layoutBoundsProperty().addListener { observableValue, oldValue, newValue ->
                        if (newValue.height > 0) {
                            shortHeight = this@NewsContent.height
                            this@NewsContent.prefHeight = shortHeight
                        }
                    }
                }
            }
            longDescription = VBox().apply {
                if (files.size() > 0) {
                    val descriptionLabel = Label(description).apply {
                        isWrapText = true
                        textFill = Color.GRAY
                    }
                    children.add(descriptionLabel)
                    layoutBoundsProperty().addListener(ChangeListener { _, oldValue, newValue ->
                        if (newValue.height > oldValue.height) {
                            longHeight = this@NewsContent.prefHeight - shortDescription.height + newValue.height
                            setAnimation()
                        }
                    })
                }
            }
        } else {
            simpleDescription = cleanDescription(description)
            shortDescription = Label(simpleDescription).apply {
                isWrapText = false
                ellipsisString = "..."
                textOverrun = OverrunStyle.ELLIPSIS
                layoutBoundsProperty().addListener { observableValue, oldValue, newValue ->
                    if (newValue.height > 0) {
                        shortHeight = this@NewsContent.height
                        this@NewsContent.prefHeight = shortHeight
                    }
                }
            }
            longDescription = cleanDescriptionVer2(description).apply {
                layoutBoundsProperty().addListener(ChangeListener { _, oldValue, newValue ->
                    if (newValue.height > oldValue.height) {
                        longHeight = this@NewsContent.prefHeight - shortDescription.height + newValue.height
                        setAnimation()
                    }
                })
            }
        }
        if (files.size() > 0) {
            longDescription.children.addAll(Separator(), Label("添付ファイル"))
            files.forEach { json ->
                json as JsonObject
                val fileName = json.getString("FileName", "")
                val hyperlink = Hyperlink(fileName).apply {
                    setOnAction {
                        val chooser = FileChooser()
                        chooser.extensionFilters.add(FileChooser.ExtensionFilter("All", "*.*"))
                        chooser.initialDirectory = File(System.getProperty("user.home"))
                        chooser.initialFileName = fileName
                        val file = chooser.showSaveDialog(null) ?: return@setOnAction
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
                longDescription.children.add(hyperlink)
            }
        }

        this.setOnMouseClicked {
            if (!showingDescription) {
                if (unread) setRead()
                this.children.remove(shortDescription)
                this.children.add(longDescription)
                showingDescription = true
            }
        }

        this.children.addAll(tagBox, topBorderPane, separator, shortDescription)
    }

    private fun setAnimation() {
        this.children.remove(longDescription)
        this.children.add(shortDescription)
        showingDescription = false
        longAnimation = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(this.prefHeightProperty(), longHeight))).apply {
            cycleCount = 1
            setOnFinished {
                this@NewsContent.children.remove(shortDescription)
                this@NewsContent.children.add(longDescription)
                animating = false
            }
        }
        longAnimation?.play()
        showingDescription = true
        this.setOnMouseClicked {
            if (animating) return@setOnMouseClicked
            animating = true
            showingDescription = if (showingDescription) {
                if (shortAnimation == null) {
                    shortAnimation = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(this.prefHeightProperty(), shortHeight))).apply {
                        cycleCount = 1
                        setOnFinished {
                            animating = false
                        }
                    }
                }
                shortAnimation?.play()
                Platform.runLater {
                    this.children.remove(longDescription)
                    this.children.add(shortDescription)
                }
                !showingDescription
            } else {
                if (unread) setRead()
                longAnimation?.play()
                !showingDescription
            }
        }
    }

    private fun setRead() {
        if (unread) {
            unread = false
            this.styleClass.remove("unread")
            this.styleClass.add("alreadyRead")
            Platform.runLater {
                tagBox.children.remove(unreadLabel)
            }
        }
    }

    enum class DescriptionType {
        SHORT,
        LONG
    }
    
    fun getDecription(): String {
        return simpleDescription
    }
}
