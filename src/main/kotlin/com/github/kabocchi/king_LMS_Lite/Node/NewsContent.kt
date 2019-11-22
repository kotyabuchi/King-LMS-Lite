package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.NewsCategory
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescription
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescriptionVer2
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.control.Separator
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import org.jsoup.nodes.Document

class NewsContent(doc: Document, _unread: Boolean, published: String, newsCategoryMap: MutableMap<Int, NewsCategory>): VBox() {

    private val json = Json.parse(doc.text()).asObject()
    private val description = doc.body().toString().split("\"Body\": \"")[1].split("\", \"SenderId\"")[0]

    private val tagBox: HBox
    private var unreadLabel: Label? = null
    private val separator = Separator()

    private var showingDescription = false

    var unread = _unread
    val category = newsCategoryMap[json.getInt("CategoryId", 1)]!!

    private val title = json.getString("Title", "")

    private var longDescription: TextFlow
    private var shortDescription: Label

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

            if (json.getString("Priority", "普通") == "緊急") {
                val priorityLabel = Label("緊急").apply {
                    textFill = Color.web("#ff4500")
                }
                children.add(priorityLabel)
            }

            val categoryLabel = Label(category.categoryName)
            children.add(categoryLabel)
        }

        val topBorderPane = BorderPane()

        separator.prefWidth = this.prefWidth / 40

        val titleText = Text(title)
        titleText.font = Font.font(Font(14.0).family, FontWeight.BOLD, 14.0)
        topBorderPane.left = titleText

        val dateText = Text("掲載日: $published")
        dateText.font = Font.font(Font(13.0).family, FontWeight.BOLD, 13.0)
        topBorderPane.right = dateText


        shortDescription = Label(cleanDescription(description).replace("\n", "")).apply {
            maxWidth = 1200.0
            prefWidth = 1200.0
            isWrapText = false
            ellipsisString = "..."
            textOverrun = OverrunStyle.ELLIPSIS
        }

        longDescription = cleanDescriptionVer2(description)

        this.setOnMouseClicked {
            showingDescription = if (showingDescription) {
                this.children.remove(longDescription)
                this.children.add(shortDescription)
                !showingDescription
            } else {
                if (unread) setRead()
                this.children.remove(shortDescription)
                this.children.add(longDescription)
                !showingDescription
            }
        }

        this.children.addAll(tagBox, topBorderPane, separator, shortDescription)
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
}
