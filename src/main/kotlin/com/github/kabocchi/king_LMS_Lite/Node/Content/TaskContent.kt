package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Node.MainPane.SettingPane
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescriptionVer2
import com.github.kabocchi.king_LMS_Lite.Utility.createHttpClient
import com.google.api.client.util.DateTime
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
import org.apache.http.client.methods.HttpGet
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class TaskContent(taskPane: TaskPane, val taskDetail: TaskDetail, googleCalendar: GoogleCalendar) : VBox() {
    val submissionEnd = taskDetail.submissionEnd
    
    private var limitHours: Long = 0
    private var longDescription: VBox
    private var openMark: Label

    init {
        this.apply {
            spacing = 4.0
            cursor = Cursor.HAND
            padding = Insets(10.0, 30.0, 10.0, 10.0)
            styleClass.add("content-box")
        }
        println("${taskDetail.title} registered: ${googleCalendar.hasEvent(taskDetail.title)}")

        if (taskDetail.hasSubmissionPeriod) {
            val today = LocalDateTime.now()
            taskDetail.submissionEnd?.let {
                when {
                    it.isBefore(today.plusDays(1)) -> {
                        this.styleClass.add("task-before-one")
                    }
                    it.isBefore(today.plusDays(2)) -> {
                        this.styleClass.add("task-before-three")
                    }
                    it.isBefore(today.plusDays(4)) -> {
                        this.styleClass.add("task-before-seven")
                    }
                    else -> {
                        this.styleClass.add("task-after-seven")
                    }
                }
            }
        } else {
            this.styleClass.add("task-after-seven")
        }

        val tagBox = HBox(10.0).apply {
            styleClass.add("tag-box")
            padding = Insets(0.0)
            setMargin(this, Insets(-2.0, 0.0, -4.0, 0.0))

            if (taskDetail.hasReSubmissionPeriod) {
                val resubmissionLabel = Label("再提出").apply {
                    textFill = Color.web("#ff4500")
                }
                children.add(resubmissionLabel)
            }
            val groupLabel = Label(taskDetail.groupName)
            val taskTypeLabel = Label(taskDetail.taskType.typeName)
            children.addAll(groupLabel, taskTypeLabel)
        }

        val topBorderPane = BorderPane()
        val titleBox = HBox(10.0)
        topBorderPane.left = titleBox

        val titleText = Label(taskDetail.title).apply {
            style = "-fx-font-weight: bold;"
        }
        titleBox.children.add(titleText)

        val dateText = Label().apply {
            text = if (taskDetail.hasSubmissionPeriod) {
                limitHours = ChronoUnit.HOURS.between(LocalDateTime.now(), taskDetail.submissionEnd?.plusHours(9))
                val limit = if (limitHours / 24 <= 0) {
                    "${limitHours}時間"
                } else {
                    "${limitHours / 24}日"
                }
                "期限 : $limit"
            } else {
                "期限 : なし"
            }
            style = "-fx-font-size: 13px; -fx-font-weight: bold;"
            styleClass.add("taskLimitDay")
        }
        topBorderPane.right = dateText

        openMark = Label("▼").apply {
            style = "-fx-font-size: 10px;"
        }

        if (taskDetail.hasDescription) {
            longDescription = cleanDescriptionVer2(taskDetail.description).apply {
                minHeight = 0.0
                var descHeight = 0.0
                var open = false
                var animating = false
                layoutBoundsProperty().addListener(ChangeListener { _, _, newValue ->
                    if (descHeight == 0.0 && newValue.height > 0.0) {
                        descHeight = newValue.height
                        val openAnim = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(maxHeightProperty(), descHeight)),
                                KeyFrame(Duration.seconds(0.2), KeyValue(openMark.rotateProperty(), 180.0))).apply {
                            cycleCount = 1
                            setOnFinished {
                                animating = false
                            }
                        }
                        val closeAnim = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(maxHeightProperty(), 0.0)),
                                KeyFrame(Duration.seconds(0.2), KeyValue(openMark.rotateProperty(), 0.0))).apply {
                            cycleCount = 1
                            setOnFinished {
                                animating = false
                            }
                        }
                        this@TaskContent.setOnMouseClicked {
                            if (animating) return@setOnMouseClicked
                            animating = true
                            if (open) {
                                closeAnim.play()
                            } else {
                                openAnim.play()
                            }
                            open = !open
                        }
                        Platform.runLater {
                            maxHeight = 0.0
                        }
                    }
                })
//                val submitButton = Button("提出").apply {
//                    styleClass.add("border-button")
//                    setOnAction {
//                        createHttpClient().use {
//                            val validateSession = HttpPost("https://king.kcg.kyoto/campus/Mvc/MasterPage/ValidateSession")
//                            it.execute(validateSession, context).use {
//                                if (it.statusLine.statusCode == HttpStatus.SC_OK) {
//                                    println(EntityUtils.toString(it.entity))
//                                    context.cookieStore.cookies.forEach {
//                                        println("${it.name} : ${it.value}")
//                                    }
//                                }
//                            }
//
//                            val postText = HttpPost("https://king.kcg.kyoto/campus/Mvc/Manavi/SaveTextReport").apply {
//                                addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
//                                addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                                addHeader("RequestVerToken", requestVerToken)
//                                addHeader("Referer", "https://king.kcg.kyoto/campus/Course/$groupId/21/")
//                            }
//
//                            val formParams = mutableListOf(
//                                    BasicNameValuePair("taskId", taskId.toString()),
//                                    BasicNameValuePair("resubmitId", "0"),
//                                    BasicNameValuePair("text", "test"),
//                                    BasicNameValuePair("gToken", groupAccessToken)
//                            )
//                            postText.entity = UrlEncodedFormEntity(formParams, "UTF-8")
//                            println(EntityUtils.toString(postText.entity))
//
//                            it.execute(postText, context).use {
//                                println(it.statusLine.statusCode)
//                                if (it.statusLine.statusCode == HttpStatus.SC_OK) {
//                                    println(EntityUtils.toString(it.entity))
//                                }
//                            }
//                        }
//                        val fileChooser = FileChooser()
//                        fileChooser.title = "提出ファイルを選択"
//                        fileChooser.showOpenDialog(null)?.let { file ->
//                            createHttpClient().use { client ->
//                                val tempFolderId = "$userId-${System.currentTimeMillis()}"
//                                val boundary = "---------------------------${Random.nextInt(0..(Int.MAX_VALUE))}"
//                                val post = HttpPost("https://king.kcg.kyoto/campus/Mvc/Manavi/SaveTempFiles")
//                                val entity = MultipartEntityBuilder.create()
//                                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
//                                        .setCharset(Charset.forName("utf-8"))
//                                        .setBoundary(boundary)
//                                        .addTextBody("tempFolderId", tempFolderId)
//                                        .addBinaryBody("files", file, ContentType.create(Tika().detect(file)), file.name)
//                                        .build()
//
//                                post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
//                                post.addHeader("Content-Type", "multipart/form-data; boundary=$boundary")
//                                post.entity = entity
//                                println(EntityUtils.toString(entity))
//
//                                client.execute(post, context).use { response ->
//                                    val responseStr = EntityUtils.toString(response.entity)
//                                    println(response.statusLine.statusCode)
//                                    println(responseStr)
//                                    if (response.statusLine.statusCode == HttpStatus.SC_OK && responseStr.trim().isEmpty()) {
//                                        createHttpClient().use { client2 ->
//                                            println(requestVerToken)
//                                            val post2 = HttpPost("https://king.kcg.kyoto/campus/Mvc/Manavi/SaveFilesReport")
//                                            post2.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
//                                            post2.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                                            post2.addHeader("RequestVerToken", requestVerToken)
//                                            post2.addHeader("X-Requested-With", "XMLHttpRequest")
//
//                                            val formParams = mutableListOf<BasicNameValuePair>()
//                                            formParams.add(BasicNameValuePair("taskId", taskId.toString()))
//                                            formParams.add(BasicNameValuePair("resubmitId", "0"))
//                                            formParams.add(BasicNameValuePair("tempFolderId", tempFolderId))
//                                            formParams.add(BasicNameValuePair("gToken", groupAccessToken))
//
//                                            val entity2 = UrlEncodedFormEntity(formParams, "UTF-8")
//                                            post2.entity = entity2
//                                            println(EntityUtils.toString(entity2))
//
//                                            client2.execute(post2, context).use { response2 ->
//                                                println(response2.statusLine.statusCode)
//                                                println(EntityUtils.toString(response2.entity))
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                children.add(submitButton)
            }
            Rectangle().apply {
                widthProperty().bind(longDescription.widthProperty())
                heightProperty().bind(longDescription.maxHeightProperty())
                longDescription.clip = this
            }
        } else {
            longDescription = VBox().apply {
                val descriptionLabel = Label(taskDetail.description).apply {
                    isWrapText = true
                    textFill = Color.GRAY
                }
                children.add(descriptionLabel)
            }
        }
        if (taskDetail.files.size() > 0) {
            longDescription.children.add(0, Separator())
            this.children.addAll(tagBox, topBorderPane, longDescription, Separator(),
                    BorderPane().apply {
                        padding = Insets(0.0, 8.0, 0.0, 0.0)
                        left = Label("添付ファイル").apply {
                            style = "-fx-font-size: 12px;"
                        }
                        if (taskDetail.hasDescription) right = openMark
                    }
            )
            taskDetail.files.forEach { fileJson ->
                fileJson as JsonObject
                val fileName = fileJson.getString("FileName", "")
                val hyperlink = Hyperlink(fileName).apply {
                    setOnAction {
                        val file = if (SettingPane.getTaskSaveSetting().askingEachTime != false) {
                            val chooser = FileChooser()
                            chooser.extensionFilters.add(FileChooser.ExtensionFilter("All", "*.*"))
                            chooser.initialDirectory = File(System.getProperty("user.home"))
                            chooser.initialFileName = fileName
                            chooser.showSaveDialog(null)
                        } else {
                            var saveFolder = File(SettingPane.getTaskSaveSetting().folderPath ?: "task/")
                            if (SettingPane.getTaskSaveSetting().saveToGroupFolder != false) {
                                saveFolder = File(saveFolder.path + File.separator + taskDetail.groupName + File.separator)
                            }
                            if (!saveFolder.exists()) saveFolder.mkdirs()
                            File(saveFolder.path + File.separator + fileName)
                        } ?: return@setOnAction
                        println("Save file($fileName) to ${file.path}")

                        createHttpClient().use { httpClient ->
                            val httpGet = HttpGet("https://king.kcg.kyoto/campus/Download/DownloadHandler.aspx?cid=${taskDetail.contentId}&docid=${fileJson.getInt("Id", 0)}")
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
                                    taskPane.changeProgressText("ファイルを保存しました [$fileName]")
                                }
                            } catch (exception: Exception) {
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
                if (taskDetail.hasDescription) right = openMark
            })
        }
    }
    
    fun registerToGoogleCalendar(googleCalendar: GoogleCalendar) {
        taskDetail.submissionEnd?.let { endTime ->
            val start = DateTime(taskDetail.submissionStart!!.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
            val end = DateTime(endTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
            val end2 = DateTime(endTime.minusMinutes(1).atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
            if (!googleCalendar.hasEvent(taskDetail.title)) {
                googleCalendar.registerEvent(CalendarType.MAIN, taskDetail.title, start, end, mutableMapOf())
            }
            if (!googleCalendar.hasEvent("${taskDetail.title} 通知用")) {
                endTime.plusHours(9).let {
                    val reminder = mutableMapOf<ReminderType, Int>()
                    val popupSetting = SettingPane.getPopupNotificationSetting()
                    val mailSetting = SettingPane.getMailNotificationSetting()
                    if (popupSetting.send == true) {
                        var notificationTime = LocalTime.parse(popupSetting.time!!.split(" ")[1])
                        if (popupSetting.time!!.startsWith("午後")) notificationTime = notificationTime.plusHours(12)
                        var notificationDateTime = it.minusDays(Integer.parseInt(popupSetting.day).toLong()).withHour(notificationTime.hour).withMinute(notificationTime.minute)
                        if (notificationDateTime.isAfter(it)) notificationDateTime = notificationDateTime.minusDays(1)
                        reminder[ReminderType.POP_UP] = (ChronoUnit.MINUTES.between(notificationDateTime, it) - 1).toInt()
                        println("[${taskDetail.title}] ポップアップ通知時間: $notificationDateTime")
                    }
                    if (mailSetting.send == true) {
                        var notificationTime = LocalTime.parse(mailSetting.time!!.split(" ")[1])
                        if (mailSetting.time!!.startsWith("午後")) notificationTime = notificationTime.plusHours(12)
                        var notificationDateTime = it.minusDays(Integer.parseInt(mailSetting.day).toLong()).withHour(notificationTime.hour).withMinute(notificationTime.minute)
                        if (notificationDateTime.isAfter(it)) notificationDateTime = notificationDateTime.minusDays(1)
                        reminder[ReminderType.E_MAIL] = (ChronoUnit.MINUTES.between(notificationDateTime, it) - 1).toInt()
                        println("[${taskDetail.title}] メール通知時間: $notificationDateTime")
                    }
                    googleCalendar.registerEvent(CalendarType.NOTIFICATION, "${taskDetail.title} 通知用", end2, end, reminder)
                }
            }
        }
    }
}