package com.github.kabocchi.king_LMS_Lite.Controller

import com.github.kabocchi.kingLmsLite.Node.NewsPane
import com.github.kabocchi.kingLmsLite.Node.TaskPane
import com.github.kabocchi.king_LMS_Lite.Node.MainPane.SettingPane
import com.github.kabocchi.king_LMS_Lite.Node.MainPane.TimeTablePane
import com.github.kabocchi.king_LMS_Lite.Utility.getDocumentWithJsoup
import com.github.kabocchi.king_LMS_Lite.Utility.toMap
import com.github.kabocchi.king_LMS_Lite.context
import com.github.kabocchi.king_LMS_Lite.main
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import org.jsoup.nodes.Document
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer

class MainController {

    @FXML lateinit var mainStackPane: StackPane
    @FXML lateinit var mainPane: BorderPane
    @FXML lateinit var news: Button
    @FXML lateinit var task: Button
    @FXML lateinit var timeTable: Button
    @FXML lateinit var setting: Button

    @FXML lateinit var closeWindow: Button

    private lateinit var newsPane: NewsPane
    private lateinit var taskPane: TaskPane
    private lateinit var timeTablePane: TimeTablePane
    private val settingPane: SettingPane = SettingPane

    private var timeTableDoc: Document? = null

    @FXML
    fun initialize() {
        timeTableDoc = getDocumentWithJsoup(context.cookieStore.toMap(), "https://king.kcg.kyoto/campus/Portal/Home") ?: return
        newsPane = NewsPane(mainStackPane)
        mainPane.center = newsPane
        taskPane = TaskPane(mainStackPane, timeTableDoc)
        timeTablePane = TimeTablePane(timeTableDoc)
        val timerTask = object : TimerTask() {
            override fun run() {
                getNews()
                getTask()
            }
        }
        Timer().schedule(timerTask, 0, 3600000)
    }

    @FXML
    fun onClickClose() {
        main?.primaryStage?.hide()
    }
    
    @FXML
    fun onClickMinimize() {
        main?.primaryStage?.isIconified = true
    }

    @FXML
    fun onClickNews() {
        mainPane.center = newsPane
    }

    @FXML
    fun onClickTask() {
        mainPane.center = taskPane
    }

    @FXML
    fun onClickTimeTable() {
        mainPane.center = timeTablePane
    }

    @FXML
    fun onClickSetting() {
        mainPane.center = settingPane
    }

    fun getNews() {
        thread {
            newsPane.updateNews()
        }
    }

    fun getTask() {
        thread {
            taskPane.updateTask()
        }
    }
}
