package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.github.kabocchi.king_LMS_Lite.Utility.getDocument
import com.github.kabocchi.king_LMS_Lite.connection
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URLEncoder
import kotlin.concurrent.thread

class TaskPane: BorderPane() {

    private val progressBar: ProgressBar
    private val progressText: Label
    private val searchBox: TextField
    private val filterButton: Button
    private val listViewButton: ToggleButton
    private val gridViewButton: ToggleButton
    private val scrollPane: ScrollPane

    private var listView = VBox()
    private var gridView = VBox()

    private var updatingTask = false

    init {
        val start = System.currentTimeMillis()

        this.style = "-fx-background-color: white;"

        listView = VBox().apply {
            spacing = 5.0
            prefWidth = 1240.0
            style =  "-fx-background-color: #fff;"
        }

        progressBar = ProgressBar()
        progressBar.prefWidth = 1280.0

        val toolBoxTopV = VBox()
        toolBoxTopV.children.add(progressBar)

        val toolBoxTopH = HBox().apply {
            id = "toolBoxTop"
            spacing = 10.0
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0, 30.0, 10.0, 30.0)
        }

        toolBoxTopV.children.add(toolBoxTopH)
        this.top = toolBoxTopV

        progressText = Label().apply {
            prefWidth = 850.0
            font = Font.font(Font(14.0).family, FontWeight.BOLD, 14.0)
        }

        searchBox = TextField().apply {
            promptText = "検索"
            prefWidth = 250.0
        }

        filterButton = Button("").apply {
            setOnAction {
                if (!updatingTask) updateTask()
            }
        }

        val viewButtonGroup = ToggleGroup()
        listViewButton = ToggleButton().apply {
            toggleGroup = viewButtonGroup
            isSelected = true
            id = "listViewButton"
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
        toolBoxTopH.children.addAll(progressText, searchBox, filterButton, listViewButton, gridViewButton)

        scrollPane = ScrollPane()
        scrollPane.prefWidth = 1240.0
        scrollPane.style = "-fx-padding: 10.0px;" + "-fx-background-color: #fff;"
        this.center = scrollPane
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
        thread {
            updatingTask = true
            val start = System.currentTimeMillis()
            Platform.runLater {
                progressBar.progress = -1.0
            }

            listView = VBox().apply {
                spacing = 5.0
                prefWidth = 1240.0
                style = "-fx-background-color: #fff;"
            }

            changeProgressText("課題の一覧を取得しています...")

            val tasks: Document = try {
                getDocument(connection, "https://king.kcg.kyoto/campus/Mvc/Home/GetDeliverables")
            } catch (e: IOException) {
                changeProgressText("課題の一覧の取得に失敗しました", true)
                endUpdate()
                return@thread
            } ?: return@thread

            val taskArray = Json.parse(tasks.text()).asArray()
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
                        val tokenGetUrl = getDocument(connection, "https://king.kcg.kyoto/campus/Course/$groupId/21/")
                        if (tokenGetUrl == null) {
                            failAmount++
                            continue
                        }
                        val token = URLEncoder.encode(tokenGetUrl.select("input[name=__GroupAccessToken]").`val`(), "UTF-8")
                        val taskDetailUrl = "https://king.kcg.kyoto/campus/Mvc/Manavi/GetTask?tId=$taskId&gToken=$token"
                        val taskDoc = getDocument(connection, taskDetailUrl)
                        if (taskDoc == null) {
                            failAmount++
                            continue
                        }
                        
                        var description = ""
                        if (taskDoc.body().toString().split("\"Description\": \"").size >= 2) description = taskDoc.body().toString().split("\"Description\": \"")[1].split("\", \"TaskBlockID\"")[0]
                        val listTaskContent = TaskContent(
                                Json.parse(taskDoc.body().text()).asObject(),
                                description,
                                json.getString("GroupName", ""),
                                groupId)
                        if (listTaskContent.getSubmissionEnd() == null) {
                            unlimitedTaskContents.add(listTaskContent)
                        } else {
                            taskContents.add(listTaskContent)
                        }
                    }
                }

                val comparator = Comparator.comparing(TaskContent::getSubmissionEnd)
                Platform.runLater {
                    for (taskContent in taskContents.stream().sorted(comparator)) {
                        listView.children.add(taskContent)
                    }
                    for (taskContent in unlimitedTaskContents) {
                        listView.children.add(taskContent)
                    }
                }
            }

            if (listViewButton.isSelected) {
                showListView()
            } else {
                showGridView()
            }


            val end = System.currentTimeMillis()
            println("GetTasks: " + (end - start).toString() + "ms")
            endUpdate()
            if (failAmount == 0) {
                changeProgressText("課題の取得が完了しました [${taskCount}件]")
            } else {
                changeProgressText("${failAmount}件の課題の取得に失敗しました", true)
            }
        }
    }

    private fun changeProgressText(text: String, error: Boolean = false) {
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
    }
}
