package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.github.kabocchi.king_LMS_Lite.Utility.getDocument
import com.github.kabocchi.king_LMS_Lite.connection
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import kotlin.concurrent.thread

class NewsPane: BorderPane() {

    private val progressBar: ProgressBar
    private val progressText: Label
    private val searchBox: TextField
    private val filterButton: Button
    private val filterBox: VBox
    private val listViewButton: ToggleButton
    private val gridViewButton: ToggleButton
    private val scrollPane: ScrollPane

    private var listView = VBox()
    private var gridView = VBox()

    private var showingFilter = false
    private var updatingNews = false

    init {
        val start = System.currentTimeMillis()
        this.style = "-fx-background-color: white;"

        val toolBoxTopV = VBox()
        progressBar = ProgressBar()
        progressBar.prefWidth = 1280.0
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

        filterBox = VBox().apply {
            spacing = 4.0
            padding = Insets(10.0, 30.0, 10.0, 30.0)
            styleClass.add("news-content-box")
        }

        val unreadOnly = CheckBox()
        val unreadOnlyText = Text("未読のみ表示する")
        val unreadFilterHBox = HBox(unreadOnly, unreadOnlyText)
        filterBox.children.add(unreadFilterHBox)

        filterButton = Button("").apply {
            setOnAction {
                showingFilter = if (showingFilter) {
                    Platform.runLater {
                        listView.children.remove(filterBox)
                    }
                    !showingFilter
                } else {
                    Platform.runLater {
                        listView.children.add(0, filterBox)
                    }
                    !showingFilter
                }
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
                if (isSelected) {
                    showListView()
                } else {
                    isSelected = true
                }
            }
        }

        gridViewButton = ToggleButton().apply {
            toggleGroup = viewButtonGroup
            id = "gridViewButton"
            prefWidth = 26.0
            prefHeight = 26.0
            setOnAction {
                if (isSelected) {
                    showGridView()
                } else {
                    isSelected = true
                }
            }
        }

        toolBoxTopH.children.addAll(progressText, searchBox, filterButton, listViewButton, gridViewButton)

        scrollPane = ScrollPane().apply {
            prefWidth = 1240.0
            style = "-fx-padding: 10.0px;" + "-fx-background-color: #fff;"
        }

        this.center = scrollPane
        val end = System.currentTimeMillis()
        println("NewsPaneInit: " + (end - start).toString() + "ms")
    }

    fun showListView() {
        Platform.runLater {
            this.center = scrollPane
            scrollPane.content = listView
        }
    }

    fun showGridView() {
        Platform.runLater {
            this.center = scrollPane
            scrollPane.content = gridView
        }
    }

    fun showError() {
        val borderPane = BorderPane()
        val reloadButton = Button()
        reloadButton.id = "reloadButton"
        borderPane.center = reloadButton
        Platform.runLater {
            scrollPane.content = borderPane
        }
    }

    fun updateNews() {
        thread {
            updatingNews = true
            val start = System.currentTimeMillis()
            Platform.runLater {
                progressBar.progress = -1.0
            }

            listView = VBox().apply {
                spacing = 5.0
                prefWidth = 1240.0
                style = "-fx-background-color: #fff;"
            }

            changeProgressText("お知らせの一覧を取得しています...")

            val doc = getDocument(connection, "https://king.kcg.kyoto/campus/Portal/TryAnnouncement/GetAnnouncements?categoryId=0&passdaysId=0&isCustomSearch=false")
            if (doc == null) {
                endUpdate()
                showError()
                return@thread
            }
            val end1 = System.currentTimeMillis()
            println("GetNewsList: " + (end1 - start).toString() + "ms")
            val jsonArray = Json.parse(doc.text()).asObject().get("data").asArray()

            var gridHBox = HBox()

            val newsAmount = jsonArray.size()

            var failAmount = 0

            for ((index, value) in jsonArray.withIndex()) {

                changeProgressText("お知らせの詳細を取得しています... [${index + 1}/$newsAmount]")

                val json = value.asObject()
                val detail = getDocument(connection, "https://king.kcg.kyoto/campus/Portal/TryAnnouncement/GetAnnouncement?aId=" + json.getInt("Id", 0))
                if (detail == null) {
                    failAmount++
                    continue
                }
                val listNewsContent = NewsContent(json, detail.body().toString().split("\"Body\": \"")[1].split("\", \"SenderId\"")[0])
                listView.children.add(listNewsContent)

//                if (index % 3 == 0) {
//                    gridHBox = HBox()
//                    gridHBox.spacing = 10.0
//                    gridView.children.add(gridHBox)
//                }
//                val newsGridContent = NewsContent(json, detail?.body().toString().split("\"Body\": \"")[1].split("\", \"SenderId\"")[0])
//                newsGridContent.prefWidth = 400.0
//                gridHBox.children.add(newsGridContent)
            }

            if (listViewButton.isSelected) {
                println("List view Selected")
                showListView()
            } else {
                showGridView()
            }


            val end2 = System.currentTimeMillis()
            println("GetNews: " + (end2 - end1).toString() + "ms")
            endUpdate()
            if (failAmount == 0) {
                changeProgressText("お知らせの取得が完了しました [${newsAmount}件]")
            } else {
                changeProgressText("${failAmount}件のお知らせの取得に失敗しました", true)
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
        updatingNews = false
    }
}
