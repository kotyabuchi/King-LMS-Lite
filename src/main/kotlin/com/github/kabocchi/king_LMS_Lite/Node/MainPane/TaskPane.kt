package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Node.Filter.TaskFilterContent
import com.github.kabocchi.king_LMS_Lite.Utility.*
import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.Duration
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.jsoup.nodes.Document
import java.io.File
import java.net.SocketException
import java.net.URLEncoder
import kotlin.concurrent.thread

class TaskPane(mainStackPane: StackPane, timetableDoc: Document?): BorderPane() {

    private val progressBar: ProgressBar
    private val progressText: Label
    private val searchBox: TextField
    private val filterButton: Button
    private val filterBox: TaskFilterContent
    private val listViewButton: ToggleButton
    private val gridViewButton: ToggleButton
    private val scrollPane: ScrollPane

    private var listView = VBox()
    private var gridView = VBox()

    private val groupList = mutableSetOf<String>()
    private val taskList = mutableListOf<TaskContent>()

    private var showingFilter = false
    private var updatingTask = false

    private val filterFadeInAnim: FadeTransition
    private val filterFadeOutAnim: FadeTransition
    private var playingFilterAnim = false

    private var endedInitialize = false

    private var userId = ""

    init {
        val start = System.currentTimeMillis()

        timetableDoc?.let {
            it.select("span.tag-timetable").forEach { element ->
                groupList.add(element.text())
            }
            it.getElementsByTag("script").forEach { element ->
                if (!element.hasAttr("src")) {
                    val split = element.toString().split("var userId = '")
                    if (split.size > 1) {
                        userId = split[1].split("';")[0].split("-")[0]
                    }
                }
            }
        }
        filterBox = TaskFilterContent(this)

        this.style = "-fx-background-color: white;"

        progressBar = ProgressBar()
        progressBar.prefWidthProperty().bind(this.widthProperty())

        val toolBoxTopV = VBox()
        toolBoxTopV.children.add(progressBar)

        val toolBoxTopH = HBox(10.0).apply {
            id = "toolBoxTop"
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0, 30.0, 10.0, 30.0)
        }

        toolBoxTopV.children.add(toolBoxTopH)
        this.top = toolBoxTopV

        progressText = Label().apply {
            prefWidthProperty().bind(this@TaskPane.widthProperty().subtract(360))
            style = "-fx-font-weight: bold;"
        }

        searchBox = TextField().apply {
            promptText = "検索"
            prefWidth = 250.0
            setOnAction {
                val searchText = text.trim()
                filterApply(title = searchText)
            }
        }

        val filterBackground = AnchorPane().apply {
            style = "-fx-background-color: rgba(150,150,150,0.8)"
            children.add(filterBox)
            filterBox.translateXProperty().bind(this.widthProperty().divide(2).subtract(filterBox.widthProperty().divide(2)))
            filterBox.translateYProperty().bind(this.heightProperty().divide(2).subtract(filterBox.heightProperty().divide(2)))
            filterBox.prefWidthProperty().bind(this.widthProperty().multiply(0.8))
            filterFadeInAnim = FadeTransition(Duration.seconds(0.1), this).apply {
                fromValue = 0.0
                toValue = 1.0
            }
            filterFadeInAnim.setOnFinished {
                playingFilterAnim = false
                showingFilter = true
            }
            filterFadeOutAnim = FadeTransition(Duration.seconds(0.1), this).apply {
                fromValue = 1.0
                toValue = 0.0
            }
            filterFadeOutAnim.setOnFinished {
                Platform.runLater {
                    mainStackPane.children.remove(this)
                    filterBox.undoFilter()
                }
                playingFilterAnim = false
                showingFilter = false
            }
            setOnMouseClicked {
                filterFadeOutAnim.play()
            }
        }

        filterButton = Button("").apply {
            styleClass.add("filter-button")
            setOnAction {
                if (playingFilterAnim) return@setOnAction
                if (showingFilter) {
                    filterFadeOutAnim.play()
                } else {
                    mainStackPane.children.add(filterBackground)
                    filterFadeInAnim.play()
                }
            }
        }

        val viewButtonGroup = ToggleGroup()
        listViewButton = ToggleButton().apply {
            toggleGroup = viewButtonGroup
            isSelected = true
            id = "listViewButton"
            minWidth = 26.0
            prefWidth = 26.0
            prefHeight = 26.0
            setOnAction {
                if (this.isSelected) {
                    showListView()
                } else {
                    this.isSelected = true
                }
            }
        }

        gridViewButton = ToggleButton().apply {
            toggleGroup = viewButtonGroup
            id = "gridViewButton"
            minWidth = 26.0
            prefWidth = 26.0
            prefHeight = 26.0
            setOnAction {
                if (this.isSelected) {
                    showGridView()
                } else {
                    this.isSelected = true
                }
            }
        }
    
        val reloadButton = Button().apply {
            styleClass.add("reload-button")
            minWidth = 26.0
            prefWidth = 26.0
            prefHeight = 26.0
            setOnAction {
                thread {
                    updateTask()
                }
            }
        }
        
        toolBoxTopH.children.addAll(progressText, searchBox, filterButton, reloadButton)

        scrollPane = ScrollPane().apply {
            isPannable = true
            isFitToWidth = true
            prefWidth = 1240.0
        }
        this.center = scrollPane

        endedInitialize = true
        val end = System.currentTimeMillis()
        println("TaskPaneInit: " + (end - start).toString() + "ms")
    }

    fun showListView() {
        Platform.runLater {
            scrollPane.content = listView
        }
    }

    fun showGridView() {
        Platform.runLater {
            scrollPane.content = gridView
        }
    }

    fun showError() {
        val reloadButton = Button()

    }

    fun updateTask() {
        if (updatingTask) return
        updatingTask = true
        main?.changePopupMenu(false, Category.TASK)

        var doMore = false
        var doCount = 0
        
        val googleCalendar = GoogleCalendar(this)
        do {
            try {
                doMore = false
                doCount++

                val start = System.currentTimeMillis()
                Platform.runLater {
                    progressBar.progress = -1.0
                    listView.children.clear()
                }
                
                taskList.clear()

                listView = VBox(8.0).apply {
                    padding = Insets(0.0, 0.0, 0.0, 10.0)
                    style = "-fx-background-color: #fff;"
                }

                changeProgressText("課題の一覧を取得しています...")

                createHttpClient().use { httpClient ->
                    val httpGet = HttpGet("https://king.kcg.kyoto/campus/Mvc/Home/GetDeliverables")
                    httpClient.execute(httpGet, context).use { taskListDoc ->
                        if (taskListDoc.statusLine.statusCode == HttpStatus.SC_OK) {
                            val taskArray = Json.parse(EntityUtils.toString(taskListDoc.entity)).asArray()

                            var taskCount = 0
                            for (task in taskArray) {
                                taskCount += task.asObject().getInt("Count", 0)
                            }

                            var failAmount = 0

                            if (taskCount > 0) {
                                val taskContents = mutableListOf<TaskContent>()
                                val unlimitedTaskContents = mutableListOf<TaskContent>()
                                var index = 0
                                for (jsonValue in taskArray) {
                                    for (task in jsonValue.asObject().get("Items").asArray()) {
                                        index++
                                        changeProgressText("課題の詳細を取得しています... [$index / $taskCount]")

                                        val json = task.asObject()

                                        val taskId = json.getInt("TaskID", 0)
                                        val groupId = json.getInt("GroupID", 0)
                                        val tokenGetUrl = getDocumentWithJsoup(context.cookieStore.toMap(), "https://king.kcg.kyoto/campus/Course/$groupId/18/")
                                        if (tokenGetUrl == null) {
                                            failAmount++
                                            continue
                                        }
                                        val requestVerToken = tokenGetUrl.select("input[name=RequestVerToken]").`val`()
                                        val groupAccessToken = tokenGetUrl.select("input[name=__GroupAccessToken]").`val`()
                                        val encodedToken = URLEncoder.encode(groupAccessToken, "UTF-8")
                                        val taskDetailUrl = "https://king.kcg.kyoto/campus/Mvc/Manavi/GetTask?tId=$taskId&gToken=$encodedToken"

                                        var retry = false
                                        var retryCount = 0
                                        do {
                                            createHttpClient().use {  httpClient2 ->
                                                try {
                                                    println("Getting task details [${index}/$taskCount]")
                                                    httpClient2.execute(HttpGet(taskDetailUrl), context).use { taskDetailDoc ->
                                                        println("Task details statusCode [${index}/$taskCount]: " + taskDetailDoc.statusLine.statusCode)
                                                        if (taskDetailDoc.statusLine.statusCode == HttpStatus.SC_OK) {
                                                            var description = ""
                                                            val docString = EntityUtils.toString(taskDetailDoc.entity)
                                                            if (docString.split("\"Description\": \"").size >= 2) description = docString.split("\"Description\": \"")[1].split("\"TaskBlockID\":")[0].trim().removeSuffix("\",")
                                                            val detailJson = Json.parse(docString).asObject()
                                                            val detailFile = File(FOLDER_PATH + "Tasks" + File.separator + detailJson.getString("Title", "") + ".json")
                                                            val listTaskDetail = if (detailFile.exists()) {
                                                                println("Create Task detail from Json")
                                                                TaskDetail(Json.parse(readFile(detailFile)).asObject())
                                                            } else {
                                                                println("Create Task detail from Json")
                                                                TaskDetail(
                                                                        detailJson,
                                                                        description,
                                                                        json.getString("GroupName", ""),
                                                                        groupId,
                                                                        requestVerToken,
                                                                        groupAccessToken,
                                                                        userId)
                                                                        .apply {
                                                                    if (!File(FOLDER_PATH + "Tasks").exists()) File(FOLDER_PATH + "Tasks").mkdirs()
                                                                    saveFile(FOLDER_PATH + "Tasks" + File.separator + this.title + ".json", this.toJson())
                                                                }
                                                            }
                                                            val listTaskContent = TaskContent(
                                                                    this,
                                                                    listTaskDetail,
                                                                    googleCalendar)
                                                            if (listTaskDetail.hasSubmissionPeriod) {
                                                                taskContents.add(listTaskContent)
                                                            } else {
                                                                unlimitedTaskContents.add(listTaskContent)
                                                            }
                                                        } else {
                                                            failAmount++
                                                        }
                                                        taskDetailDoc.close()
                                                    }
                                                } catch (e: SocketException) {
                                                    println("Fails to get task details [${index}/$taskCount]")
                                                    if (e.message == "Connection reset") {
                                                        retry = true
                                                        retryCount++
                                                        println("Try to get task details again [${index}/$taskCount]")
                                                    }
                                                }
                                            }
                                        } while (retry && retryCount <= 5)
                                    }
                                }

                                val comparator = Comparator.comparing(TaskContent::submissionEnd)
                                Platform.runLater {
                                    for (taskContent in taskContents.stream().sorted(comparator)) {
                                        taskList.add(taskContent)
                                    }
                                    for (taskContent in unlimitedTaskContents) {
                                        taskList.add(taskContent)
                                    }
                                }
                                filterApply()
                            }


                            if (listViewButton.isSelected) {
                                showListView()
                            } else {
                                showGridView()
                            }


                            val end = System.currentTimeMillis()
                            println("GetTasks: " + (end - start).toString() + "ms")
                            if (failAmount == 0) {
                                changeProgressText("課題の取得が完了しました [${taskCount}件]")
                            } else {
                                changeProgressText("${taskCount}件中${failAmount}件の課題の取得に失敗しました", true)
                            }
                        } else {
                            changeProgressText("課題の一覧の取得に失敗しました", true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message?.startsWith("Connection reset") == true) doMore = true
            } finally {
                endUpdate()
            }
        } while (doMore && doCount < 5)
        thread {
            taskList.forEach {
                it.registerToGoogleCalendar(googleCalendar)
            }
        }
    }

    fun changeProgressText(text: String, error: Boolean = false) {
        Platform.runLater {
            progressText.text = text
            if (error) progressText.textFill = Color.web("#e12929") else Color.BLACK
        }
    }

    private fun endUpdate() {
        Platform.runLater {
            progressBar.progress = 0.0
        }
        updatingTask = false
        main?.changePopupMenu(true, Category.TASK)
    }

    fun getGroupList(): Set<String> {
        return groupList
    }

    fun filterApply(msg: Boolean = false, title: String = "") {
        Platform.runLater {
            listView.children.clear()
            var count = 0
            for (it in taskList) {
                if (title != "" && (!it.taskDetail.title.contains(title, true) && !it.taskDetail.simpleDescription.contains(title, true))) continue
                if (filterBox.isResubmissionOnly() && !it.taskDetail.hasReSubmissionPeriod) continue
                if (filterBox.getTypeFilter()[it.taskDetail.taskType]?.isSelected == false) continue
                if (filterBox.getGroupFilter()[it.taskDetail.groupName]?.isSelected == false) continue
                listView.children.add(it)
                count++
            }
            if (msg) changeProgressText("フィルターを適応しました [${count}件]")
        }
    }
}
