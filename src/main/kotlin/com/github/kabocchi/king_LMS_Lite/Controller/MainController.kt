package com.github.kabocchi.king_LMS_Lite.Controller

import com.github.kabocchi.kingLmsLite.Node.NewsPane
import com.github.kabocchi.kingLmsLite.Node.TaskPane
import com.github.kabocchi.king_LMS_Lite.Node.SettingPane
import com.github.kabocchi.king_LMS_Lite.Node.TimeTablePane
import com.github.kabocchi.king_LMS_Lite.Utility.getDocument
import com.github.kabocchi.king_LMS_Lite.connection
import com.github.kabocchi.king_LMS_Lite.main
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import org.jsoup.nodes.Document

class MainController {

    @FXML lateinit var mainPane: BorderPane
    @FXML lateinit var news: Button
    @FXML lateinit var task: Button
    @FXML lateinit var timeTable: Button
    @FXML lateinit var setting: Button

    @FXML lateinit var closeWindow: Button

    private lateinit var newsPane: NewsPane
    private lateinit var taskPane: TaskPane
    private lateinit var timeTablePane: TimeTablePane
    private lateinit var settingPane: SettingPane

    private var timeTableDoc: Document? = null

    @FXML
    fun initialize() {
        timeTableDoc = getDocument(connection, "https://king.kcg.kyoto/campus/Portal/Home")
        newsPane = NewsPane()
        mainPane.center = newsPane
        taskPane = TaskPane(timeTableDoc)
        timeTablePane = TimeTablePane(timeTableDoc)
        settingPane = SettingPane()
        getNews()
        getTask()
        println(mainPane.width)
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
        newsPane.updateNews()
    }

    fun getTask() {
        taskPane.updateTask()
    }
}
