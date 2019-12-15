package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.TaskType
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
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.util.Duration
import org.apache.http.HttpStatus
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.apache.tika.Tika
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.random.nextInt

class TaskContent(json: JsonObject, _description: String, _groupName: String, groupId: Int, requestVerToken: String, groupAccessToken: String, userId: String): VBox() {
    private val separator = Separator()

    private var showingDescription = false

    private val title = json.getString("Title", "")
    private val contentId = json.getString("ContentID", "")
    private val taskId = json.getInt("TaskID", 0)
    private val files = json.get("backgrounds")?.asArray() ?: JsonArray()
    private val linkUrl: String = "https://king.kcg.kyoto/campus/Course/$groupId/21/#/detail/${json.getInt("TaskBlockID", 0)}/$taskId"
    private var strSubmissionStart = ""
    private var strSubmissionEnd = ""
    private var strLastUpdate: String = json.getString("UpdatedDate", "")
    private var submissionStart: LocalDateTime? = null
    private var submissionEnd: LocalDateTime? = null
    private var limitHours: Long = 0

    private var longDescription: VBox
    private var shortDescription: Label

    private var shortHeight = 0.0
    private var longHeight = 0.0

    private var shortAnimation: Timeline? = null
    private var longAnimation: Timeline? = null

    private var animating = true

    val groupName = _groupName
    var resubmission = false
    val taskType: TaskType

    init {
        if (json.toString().split("\"SubmissionStart\":\"").size >= 2) {
            strSubmissionStart = json.getString("SubmissionStart", "")
            submissionStart = LocalDateTime.parse(strSubmissionStart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
        }
        if (json.get("ReSubmissions").asArray().size() >= 1) {
            val resubmissions = json.get("ReSubmissions").asArray()[0] as JsonObject
            strSubmissionEnd = resubmissions.getString("EndDate", "")
            submissionEnd = LocalDateTime.parse(strSubmissionEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
            resubmission = true
        } else if (json.toString().split("\"SubmissionEnd\":\"").size >= 2) {
            strSubmissionEnd = json.getString("SubmissionEnd", "")
            submissionEnd = LocalDateTime.parse(strSubmissionEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
        }

        taskType = when (json.getInt("TaskType", 14)) {
            14 -> {
                if (json.getBoolean("IsTextReport", false)) {
                    TaskType.REPORT
                } else {
                    TaskType.TEXT_REPORT
                }
            }
            10 -> TaskType.TEST
            else -> TaskType.REPORT
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

        val tagBox = HBox().apply {
            styleClass.add("tag-box")
            spacing = 10.0
            padding = Insets(0.0)
            setMargin(this, Insets(-2.0, 0.0, -4.0, 0.0))

            if (resubmission) {
                val resubmissionLabel = Label("再提出").apply {
                    textFill = Color.web("#ff4500")
                }
                children.add(resubmissionLabel)
            }
            val groupLabel = Label(groupName)
            val taskTypeLabel = Label(taskType.typeName)
            children.addAll(groupLabel, taskTypeLabel)

        }

        val topBorderPane = BorderPane()
        val titleBox = HBox().apply {
            spacing = 10.0
        }
        topBorderPane.left = titleBox

        separator.prefWidth = this.prefWidth / 40

        val titleText = Text(title).apply {
            font = Font.font(Font(14.0).family, FontWeight.BOLD, 14.0)
        }
        titleBox.children.add(titleText)

        val dateText = Label().apply {
            text = if (strSubmissionEnd == "") {
                "期限: なし"
            } else {
                limitHours = ChronoUnit.HOURS.between(LocalDateTime.now(), submissionEnd)
                val limit = if (limitHours / 24 <= 0) {
                    "${limitHours}時間"
                } else {
                    "${limitHours / 24}日"
                }
                "期限: $limit"
            }
            font = Font.font(Font(13.0).family, FontWeight.BOLD, 13.0)
            styleClass.add("taskLimitDay")
        }
        topBorderPane.right = dateText

        var description = _description
        if (description.trim().isBlank()) {
            description = "このタスクには詳細文が設定されていません"
            shortDescription = Label(description).apply {
                isWrapText = false
                ellipsisString = "..."
                textOverrun = OverrunStyle.ELLIPSIS
                textFill = Color.GRAY
                if (files.size() > 0) {
                    layoutBoundsProperty().addListener { observableValue, oldValue, newValue ->
                        if (newValue.height > 0) {
                            shortHeight = this@TaskContent.height
                            this@TaskContent.prefHeight = shortHeight
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
                            longHeight = this@TaskContent.prefHeight - shortDescription.height + newValue.height
                            setAnimation()
                        }
                    })
                }
            }
        } else {
            shortDescription = Label(cleanDescription(description).replace("\n", "")).apply {
                isWrapText = false
                ellipsisString = "..."
                textOverrun = OverrunStyle.ELLIPSIS
                layoutBoundsProperty().addListener { observableValue, oldValue, newValue ->
                    if (newValue.height > 0) {
                        shortHeight = this@TaskContent.height
                        this@TaskContent.prefHeight = shortHeight
                    }
                }
            }
            longDescription = cleanDescriptionVer2(description).apply {
                layoutBoundsProperty().addListener(ChangeListener { _, oldValue, newValue ->
                    if (newValue.height > oldValue.height) {
                        longHeight = this@TaskContent.prefHeight - shortDescription.height + newValue.height
                        setAnimation()
                    }
                })
                val submitButton = Button("提出").apply {
                    styleClass.add("border-button")
                    setOnAction {
                        val fileChooser = FileChooser()
                        fileChooser.title = "提出ファイルを選択"
                        fileChooser.showOpenDialog(null)?.let { file ->
                            createHttpClient().use { client ->
                                val tempFolderId = "$userId-${System.currentTimeMillis()}"
                                val boundary = "---------------------------${Random.nextInt(0..(Int.MAX_VALUE))}"
                                val post = HttpPost("https://king.kcg.kyoto/campus/Mvc/Manavi/SaveTempFiles")
                                val entity = MultipartEntityBuilder.create()
                                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                                        .setCharset(Charset.forName("utf-8"))
                                        .setBoundary(boundary)
                                        .addTextBody("tempFolderId", tempFolderId)
                                        .addBinaryBody("files", file, ContentType.create(Tika().detect(file)), file.name)
                                        .build()

                                post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                                post.addHeader("Content-Type", "multipart/form-data; boundary=$boundary")
                                post.entity = entity
                                println(EntityUtils.toString(entity))

                                client.execute(post, context).use { response ->
                                    val responseStr = EntityUtils.toString(response.entity)
                                    println(response.statusLine.statusCode)
                                    println(responseStr)
                                    if (response.statusLine.statusCode == HttpStatus.SC_OK && responseStr.trim().isEmpty()) {
                                        createHttpClient().use { client2 ->
                                            println(requestVerToken)
                                            val post2 = HttpPost("https://king.kcg.kyoto/campus/Mvc/Manavi/SaveFilesReport")
                                            post2.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                                            post2.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                            post2.addHeader("RequestVerToken", requestVerToken)
                                            post2.addHeader("X-Requested-With", "XMLHttpRequest")

                                            val formParams = mutableListOf<BasicNameValuePair>()
                                            formParams.add(BasicNameValuePair("taskId", taskId.toString()))
                                            formParams.add(BasicNameValuePair("resubmitId", "0"))
                                            formParams.add(BasicNameValuePair("tempFolderId", tempFolderId))
                                            formParams.add(BasicNameValuePair("gToken", groupAccessToken))

                                            val entity2 = UrlEncodedFormEntity(formParams, "UTF-8")
                                            post2.entity = entity2
                                            println(EntityUtils.toString(entity2))

                                            client2.execute(post2, context).use { response2 ->
                                                println(response2.statusLine.statusCode)
                                                println(EntityUtils.toString(response2.entity))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                children.add(submitButton)
            }

            if (files.size() > 0) {
                longDescription.children.addAll(Separator(), Label("添付ファイル"))
                files.forEach { fileJson ->
                    fileJson as JsonObject
                    val hyperlink = Hyperlink(fileJson.getString("FileName", "")).apply {
                        setOnAction {
                            createHttpClient().use { httpClient ->
                                val httpGet = HttpGet("https://king.kcg.kyoto/campus/Download/DownloadHandler.aspx?cid=$contentId&docid=${fileJson.getInt("Id", 0)}")
                                try {
                                    httpClient.execute(httpGet, context).use {
                                        val inputStream = it.entity.content
                                        val filePath = fileJson.getString("FileName", "")
                                        val fileOutputStream = FileOutputStream(File(filePath))

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
        }

        this.setOnMouseClicked {
            if (!showingDescription) {
                this.children.remove(shortDescription)
                this.children.add(longDescription)
                showingDescription = true
            }
        }

        this.children.addAll(tagBox, topBorderPane, separator, shortDescription)
    }

    fun getSubmissionEnd(): LocalDateTime? {
        return submissionEnd
    }

    private fun setAnimation() {
        this.children.remove(longDescription)
        this.children.add(shortDescription)
        showingDescription = false
        longAnimation = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(this.prefHeightProperty(), longHeight))).apply {
            cycleCount = 1
            setOnFinished {
                this@TaskContent.children.remove(shortDescription)
                this@TaskContent.children.add(longDescription)
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
                longAnimation?.play()
                !showingDescription
            }
        }
    }
}
