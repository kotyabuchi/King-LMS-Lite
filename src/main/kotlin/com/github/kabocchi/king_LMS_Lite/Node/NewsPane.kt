package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.github.kabocchi.king_LMS_Lite.NewsCategory
import com.github.kabocchi.king_LMS_Lite.Node.NewsFilterContent
import com.github.kabocchi.king_LMS_Lite.Utility.getDocument
import com.github.kabocchi.king_LMS_Lite.connection
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlin.concurrent.thread

class NewsPane: BorderPane() {

    private val progressBar: ProgressBar
    private val progressText: Label
    private val searchBox: TextField
    private val filterButton: Button
    private val filterBox = NewsFilterContent(this)
    private val listViewButton: ToggleButton
    private val gridViewButton: ToggleButton
    private val scrollPane: ScrollPane

    private var listView = VBox()
    private var gridView = VBox()

    private val newsList = mutableListOf<NewsContent>()

    private var showingFilter = false
    private var updatingNews = false

    val newsCategoryMap = mutableMapOf<Int, NewsCategory>()

    private var mousePressed = false

    init {
        NewsCategory.values().forEach {
            newsCategoryMap[it.id] = it
        }

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
            style = "-fx-font-weight: bold; -fx-font-size: 14px;"
//            font = Font.font(Font(14.0).family, FontWeight.BOLD, 14.0)
            println("Font is " + font.family)
        }

        searchBox = TextField().apply {
            promptText = "検索"
            prefWidth = 250.0
        }

        filterButton = Button("").apply {
            styleClass.add("filter-button")
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
            isPannable = true
            prefWidth = 1240.0
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
                val newsContent = NewsContent(detail, !json.getBoolean("IsRead", false), json.getString("Published", ""), newsCategoryMap)
                newsList.add(newsContent)

//                if (index % 3 == 0) {
//                    gridHBox = HBox()
//                    gridHBox.spacing = 10.0
//                    gridView.children.add(gridHBox)
//                }
//                val newsGridContent = NewsContent(json, detail?.body().toString().split("\"Body\": \"")[1].split("\", \"SenderId\"")[0])
//                newsGridContent.prefWidth = 400.0
//                gridHBox.children.add(newsGridContent)
            }

            filterApply()

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

    fun filterApply() {
        Platform.runLater {
            listView.children.clear()
            if (showingFilter) listView.children.add(filterBox)
            val unreadOnly = filterBox.showUnreadOnly()
            val emergency = filterBox.showEmergency()
            val important = filterBox.showImportant()
            for (it in newsList) {
                if (unreadOnly && !it.unread) continue
                if ((emergency && it.emergency) || (important && it.important) && filterBox.categoryFilter(it.category)) {
                    listView.children.add(it)
                    continue
                }
                if (!emergency && !important && filterBox.categoryFilter(it.category)) listView.children.add(it)
            }
            showListView()
        }
    }

    private fun showAllNews() {
        Platform.runLater {
            listView.children.clear()
            if (showingFilter) listView.children.add(filterBox)
            listView.children.addAll(newsList)
        }
    }
}
