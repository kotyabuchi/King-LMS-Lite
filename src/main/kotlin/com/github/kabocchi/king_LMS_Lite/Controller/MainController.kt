package com.github.kabocchi.king_LMS_Lite.Controller

import com.github.kabocchi.kingLmsLite.Node.NewsPane
import com.github.kabocchi.kingLmsLite.Node.TaskPane
import com.github.kabocchi.king_LMS_Lite.main
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane

class MainController {

    @FXML lateinit var mainPane: BorderPane
    @FXML lateinit var news: Button
    @FXML lateinit var task: Button
    @FXML lateinit var setting: Button

    @FXML lateinit var closeWindow: Button

    private lateinit var newsPane: NewsPane
    private lateinit var taskPane: TaskPane

    @FXML
    fun initialize() {
        newsPane = NewsPane()
        mainPane.center = newsPane
        taskPane = TaskPane()
        getNews()
//        getTask()
        println(mainPane.width)
    }

    @FXML
    fun onClickClose() {
        main?.primaryStage?.hide()
    }

    @FXML
    fun onClickNews() {
        mainPane.center = newsPane
    }

    @FXML
    fun onClickTask() {
        mainPane.center = taskPane
    }

    fun getNews() {
        newsPane.updateNews()
    }

    fun getTask() {
        taskPane.updateTask()
    }
}
