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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskContent(json: JsonObject, _description: String, groupName: String, groupId: Int): VBox() {
    private val separator = Separator()

    private var showingDescription = false

    private val title = json.getString("Title", "")
    private val linkUrl: String = "https://king.kcg.kyoto/campus/Course/$groupId/21/#/detail/${json.getInt("TaskBlockID", 0)}/${json.getInt("TaskID", 0)}"
    private var strSubmissionStart = ""
    private var strSubmissionEnd = ""
    private var strLastUpdate: String = json.getString("UpdatedDate", "")
    private var submissionStart: LocalDateTime? = null
    private var submissionEnd: LocalDateTime? = null

    private var longDescription: TextFlow
    private var shortDescription: Label

    init {
        if (json.toString().split("\"SubmissionStart\":\"").size >= 2) {
            strSubmissionStart = json.getString("SubmissionStart", "")
            submissionStart = LocalDateTime.parse(strSubmissionStart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
        }
        if (json.toString().split("\"SubmissionEnd\":\"").size >= 2) {
            strSubmissionEnd = json.getString("SubmissionEnd", "")
            submissionEnd = LocalDateTime.parse(strSubmissionEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
        }
        
        this.spacing = 4.0
        this.cursor = Cursor.HAND
        this.padding = Insets(10.0, 30.0, 10.0, 30.0)
        this.styleClass.add("task-content-box")

        if (submissionEnd != null) {
            val today = LocalDateTime.now()
            when {
                submissionEnd!!.isBefore(today.plusDays(1)) -> {
                    this.styleClass.add("task-before-one")
                }
                submissionEnd!!.isBefore(today.plusDays(2)) -> {
                    this.styleClass.add("task-before-three")
                }
                submissionEnd!!.isBefore(today.plusDays(4)) -> {
                    this.styleClass.add("task-before-seven")
                }
                else -> {
                    this.styleClass.add("task-after-seven")
                }
            }
        } else {
            this.styleClass.add("task-after-seven")
        }

        val topBorderPane = BorderPane()
        this.children.add(topBorderPane)
        val titleBox = HBox()
        titleBox.spacing = 10.0

        separator.prefWidth = this.prefWidth / 40

        val titleText = Text(title).apply {
            font = Font.font(Font(14.0).family, FontWeight.BOLD, 14.0)
        }
        titleBox.children.add(titleText)
        topBorderPane.left = titleBox

        this.children.add(separator)

        var description = _description
        if (description.trim().isBlank()) {
            description = "このタスクには詳細文が設定されていません"
            shortDescription = Label(description).apply {
                maxWidth = 1200.0
                prefWidth = 1200.0
                isWrapText = false
                ellipsisString = "..."
                textOverrun = OverrunStyle.ELLIPSIS
                textFill = Color.GRAY
            }
            this.children.add(shortDescription)
            longDescription = TextFlow()
        } else {
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
                    this.children.remove(shortDescription)
                    this.children.add(longDescription)
                    true
                }
            }
        }
    }

    fun getSubmissionEnd(): LocalDateTime? {
        return submissionEnd
    }
}
