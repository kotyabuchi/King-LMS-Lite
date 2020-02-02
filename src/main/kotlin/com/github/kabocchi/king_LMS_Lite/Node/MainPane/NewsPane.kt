package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.Json
import com.eclipsesource.json.ParseException
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Node.Filter.NewsFilterContent
import com.github.kabocchi.king_LMS_Lite.Utility.loginToKINGLMS
import com.github.kabocchi.king_LMS_Lite.Utility.readFile
import com.github.kabocchi.king_LMS_Lite.Utility.saveFile
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
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.concurrent.thread

class NewsPane(mainStackPane: StackPane): BorderPane() {

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

    private val filterFadeInAnim: FadeTransition
    private val filterFadeOutAnim: FadeTransition
    private var playingFilterAnim = false

    private var lastUpdateTime: LocalDateTime? = null

    init {
        val start = System.currentTimeMillis()
        this.styleClass.add("news-pane")
//        this.style = "-fx-background-color: white;"

        val toolBoxTopV = VBox()
        progressBar = ProgressBar()
        progressBar.prefWidthProperty().bind(this.widthProperty())
        toolBoxTopV.children.add(progressBar)

        val toolBoxTopH = HBox(10.0).apply {
            id = "toolBoxTop"
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0, 30.0, 10.0, 30.0)
        }

        toolBoxTopV.children.add(toolBoxTopH)
        this.top = toolBoxTopV

        progressText = Label().apply {
            prefWidthProperty().bind(this@NewsPane.widthProperty().subtract(360))
            style = "-fx-font-weight: bold; -fx-font-size: 14px;"
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
            minWidth = 26.0
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
        
        val reloadButton = Button().apply {
            styleClass.add("reload-button")
            minWidth = 26.0
            prefWidth = 26.0
            prefHeight = 26.0
            setOnAction {
                thread {
                    updateNews()
                }
            }
        }

        toolBoxTopH.children.addAll(progressText, searchBox, filterButton, reloadButton)

        scrollPane = ScrollPane().apply {
            isPannable = true
            isFitToWidth = true
            prefWidthProperty().bind(this@NewsPane.widthProperty())
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
        val start = System.currentTimeMillis()

        if (updatingNews) return
        if (lastUpdateTime != null && ChronoUnit.MINUTES.between(lastUpdateTime, LocalDateTime.now()) < 5) {
            changeProgressText("更新には３分以上の間を空けてください (残り${ChronoUnit.SECONDS.between(lastUpdateTime, LocalDateTime.now())})", true)
            return
        }

        updatingNews = true
        main?.changePopupMenu(false, Category.NEWS)

        var doMore = false
        var doCount = 0

        do {
            try {
                doMore = false
                doCount++

                Platform.runLater {
                    progressBar.progress = -1.0
                    listView.children.clear()
                }
                newsList.clear()

                listView = VBox(8.0).apply {
                    padding = Insets(0.0, 0.0, 0.0, 10.0)
                    style = "-fx-background-color: transparent;"
                }

                changeProgressText("お知らせの一覧を取得しています...")

                HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setConnectionManager(PoolingHttpClientConnectionManager()).build().use { httpClient ->
                    val httpGet = HttpGet("https://king.kcg.kyoto/campus/Portal/TryAnnouncement/GetAnnouncements?categoryId=0&passdaysId=0&isCustomSearch=false")
                    httpClient.execute(httpGet, context).use { newsListDoc ->
                        println("News List StatusCode: " + newsListDoc.statusLine.statusCode)
                        if (newsListDoc.statusLine.statusCode == HttpStatus.SC_OK) {
                            try {
                                val jsonArray = Json.parse(EntityUtils.toString(newsListDoc.entity)).asObject().get("data").asArray()

                                val newsAmount = jsonArray.size()

                                var failAmount = 0

                                for ((index, value) in jsonArray.withIndex()) {

                                    changeProgressText("お知らせの詳細を取得しています... [${index + 1}/$newsAmount]")

                                    val json = value.asObject()
                                    val id = json.getInt("Id", 0)

                                    val detailFile = File(FOLDER_PATH + "News" + File.separator + id + ".json")

                                    if (detailFile.exists()) {
                                        val newsDetail = NewsDetail(Json.parse(readFile(detailFile)).asObject())
                                        val newsContent = NewsContent(this, newsDetail)
                                        newsList.add(newsContent)
                                    } else {
                                        httpClient.execute(HttpGet("https://king.kcg.kyoto/campus/Portal/TryAnnouncement/GetAnnouncement?aId=$id"), context).use { newsDetailDoc ->
                                            println("News Content StatusCode [${index + 1}/$newsAmount]: " + newsDetailDoc.statusLine.statusCode)
                                            try {
                                                if (newsDetailDoc.statusLine.statusCode == HttpStatus.SC_OK) {
                                                    val newsDetail = NewsDetail(
                                                            EntityUtils.toString(newsDetailDoc.entity),
                                                            json.getBoolean("IsRead", false),
                                                            json.getString("Published", ""))
                                                    if (!File(FOLDER_PATH + "News").exists()) File(FOLDER_PATH + "News").mkdirs()
                                                    saveFile(detailFile.path, newsDetail.toJson())
                                                    val newsContent = NewsContent(this, newsDetail)
                                                    newsList.add(newsContent)
                                                } else {
                                                    failAmount++
                                                }
                                            } catch (e2: ParseException) {
                                                failAmount++
                                            }
                                            newsDetailDoc.close()
                                        }
                                    }
                                }

                                filterApply()

                                if (listViewButton.isSelected) {
                                    println("List view Selected")
                                    showListView()
                                } else {
                                    showGridView()
                                }

                                val end2 = System.currentTimeMillis()
                                println("GetNews: " + (end2 - start).toString() + "ms")
                                endUpdate()
                                if (failAmount == 0) {
                                    changeProgressText("お知らせの取得が完了しました [${newsAmount}件]")
                                    lastUpdateTime = LocalDateTime.now()
                                } else {
                                    changeProgressText("${newsAmount}件中${failAmount}件のお知らせの取得に失敗しました", true)
                                }
                            } catch (e: ParseException) {
                                changeProgressText("お知らせ一覧の取得に失敗しました。 再取得します。 [${doCount}回目]", true)
                                e.printStackTrace()
                                if (loginToKINGLMS(loadFromFile = true) == LoginResult.SUCCESS) {
                                    changeProgressText("ログインに成功しました。")
                                    doMore = true
                                }
                            }
                        } else {
                            endUpdate()
                            showError()
                            return
                        }
                    }
                    httpClient.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is ParseException -> {
                        if (loginToKINGLMS(loadFromFile = true) == LoginResult.SUCCESS) {
                            println("Login Success!!")
                            doMore = true
                        }
                    }
                }
                if (e.message?.startsWith("Connection reset") == true) doMore = true
            }
        } while (doMore && doCount < 5)
        val end = System.currentTimeMillis()
        println("Ended News Update: " + (end - start).toString() + "ms")
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
        updatingNews = false
        main?.changePopupMenu(true, Category.NEWS)
    }

    fun filterApply(msg: Boolean = false, title: String = "") {
        Platform.runLater {
            listView.children.clear()
            val unreadOnly = filterBox.isUnreadOnly()
            val emergency = filterBox.showEmergency()
            val important = filterBox.showImportant()
            var count = 0
            for (it in newsList) {
                if (title != "" && (!it.newsDetail.title.contains(title, true) && !it.newsDetail.simpleDescription.contains(title, true))) continue
                if (unreadOnly && it.newsDetail.isRead) continue
                if ((emergency && it.newsDetail.isEmergency) || (important && it.newsDetail.isImportant) && filterBox.categoryFilter(it.newsDetail.category)) {
                    listView.children.add(it)
                    count++
                    continue
                }
                if (!emergency && !important && filterBox.categoryFilter(it.newsDetail.category)) {
                    listView.children.add(it)
                    count++
                }
            }
            showListView()
            if (msg) changeProgressText("フィルターを適応しました [${count}件]")
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