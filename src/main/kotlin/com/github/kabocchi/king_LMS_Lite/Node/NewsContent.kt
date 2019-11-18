package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescription
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescriptionVer2
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

class NewsContent(json: JsonObject, description: String): VBox() {

    private val separator = Separator()

    private var showingDescription = false

    private var isRead = json.getBoolean("IsRead", false)

    private val title = json.getString("Title", "")

    private var longDescription: TextFlow
    private var shortDescription: Label

    init {
        this.spacing = 4.0
        this.cursor = Cursor.HAND
        this.padding = Insets(10.0, 30.0, 10.0, 30.0)
        this.styleClass.add("news-content-box")
        val topBorderPane = BorderPane()
        this.children.add(topBorderPane)
        val titleBox = HBox()
        titleBox.spacing = 10.0

        separator.prefWidth = this.prefWidth / 40

        if (isRead) {
            this.styleClass.add("is-read")
        } else {
            this.styleClass.add("not-read")
            val notRead = Label("[未読]")
            notRead.font = Font.font(Font(14.0).family, FontWeight.BOLD, 14.0)
            notRead.textFill = Color.web("#e12929")
            titleBox.children.add(notRead)
        }

        val titleText = Text(title)
        titleText.font = Font.font(Font(14.0).family, FontWeight.BOLD, 14.0)
        titleBox.children.add(titleText)
        topBorderPane.left = titleBox

        val dateText = Text("掲載日: " + json.getString("Published", ""))
        dateText.font = Font.font(Font(13.0).family, FontWeight.BOLD, 13.0)
        topBorderPane.right = dateText

        this.children.add(separator)

        shortDescription = Label(cleanDescription(description).replace("\n", "")).apply {
            maxWidth = 1200.0
            prefWidth = 1200.0
            isWrapText = false
            ellipsisString = "..."
            textOverrun = OverrunStyle.ELLIPSIS
        }
        this.children.add(shortDescription)

        longDescription = cleanDescriptionVer2(description)

        this.setOnMouseClicked {
            showingDescription = if (showingDescription) {
                this.children.remove(longDescription)
                this.children.add(shortDescription)
                false
            } else {
                if (!isRead) setRead()
                this.children.remove(shortDescription)
                this.children.add(longDescription)
                true
            }
        }
    }

    private fun setRead() {
        this.styleClass.remove("not-read")
        this.styleClass.add("is-read")
    }
}
