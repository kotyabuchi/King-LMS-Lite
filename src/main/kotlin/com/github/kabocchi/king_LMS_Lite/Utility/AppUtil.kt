package com.github.kabocchi.king_LMS_Lite.Utility

import com.github.kabocchi.king_LMS_Lite.Main
import javafx.fxml.FXMLLoader
import javafx.fxml.LoadException
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle

class AppUtil(private val version: String, private val main: Main) {

    fun showLogin(stage: Stage) {
        stage.icons.add(Image(ClassLoader.getSystemResourceAsStream("logo.png")))
        val fxmlLoader = FXMLLoader(javaClass.getResource("/Login.fxml"))
        try {
            val scene = Scene(fxmlLoader.load())
            stage.title = "King-lms Lite [Login] $version"
            stage.scene = scene
            stage.show()
            main.primaryStage = stage
//            main.mainController = null
//            changePopupMenu(false)
        } catch (e: LoadException) {
            e.printStackTrace()
        }
    }

    fun showMain(stage: Stage) {
        var xOffset = 0.0
        var yOffset = 0.0
        stage.initStyle(StageStyle.UNDECORATED)
        stage.isResizable = false
        stage.icons.add(Image(ClassLoader.getSystemResourceAsStream("logo.png")))
        val fxmlLoader = FXMLLoader(javaClass.getResource("/Main2.fxml"))
        try {
            val scene = Scene(fxmlLoader.load(), 1280.0, 720.0)
            scene.stylesheets.add(javaClass.getResource("/main2.css").toExternalForm())
            stage.title = "King-lms Lite $version"
            stage.scene = scene
            stage.show()

            main.primaryStage = stage
//            changePopupMenu(true)

            scene.setOnMousePressed {
                xOffset = it.sceneX
                yOffset = it.sceneY
            }

            scene.setOnMouseDragged {
                stage.x = it.screenX - xOffset
                stage.y = it.screenY - yOffset
            }
        } catch (e: LoadException) {
            e.printStackTrace()
            println(e.message)
            println(e.cause.toString())
        }
    }
}
