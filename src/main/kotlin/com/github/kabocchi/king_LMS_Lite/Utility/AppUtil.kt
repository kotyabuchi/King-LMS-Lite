package com.github.kabocchi.king_LMS_Lite.Utility

import com.github.kabocchi.king_LMS_Lite.Category
import com.github.kabocchi.king_LMS_Lite.Main
import javafx.fxml.FXMLLoader
import javafx.fxml.LoadException
import javafx.scene.Scene
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

class AppUtil(private val version: String, private val main: Main) {

    fun showLogin(stage: Stage) {
        stage.icons.add(Image(ClassLoader.getSystemResourceAsStream("logo.png")))
        stage.isResizable = true
        val fxmlLoader = FXMLLoader(javaClass.getResource("/Login.fxml"))
        try {
            val scene = Scene(fxmlLoader.load())
            stage.title = "King-lms Lite [Login] $version"
            stage.scene = scene
            stage.show()
            main.primaryStage = stage
            main.mainController = null
            main.changePopupMenu(false, Category.NEWS, Category.TASK)
        } catch (e: LoadException) {
            e.printStackTrace()
        }
    }

    fun showMain(stage: Stage) {
        var xOffset = 0.0
        var yOffset = 0.0
        stage.initStyle(StageStyle.UNDECORATED)
        stage.isResizable = true
        stage.icons.add(Image(ClassLoader.getSystemResourceAsStream("logo.png")))
        val fxmlLoader = FXMLLoader(javaClass.getResource("/Main2.fxml"))
        try {
//            val scene = Scene(fxmlLoader.load(), 1328.0, 768.0)
            val scene = Scene(fxmlLoader.load(), 1280.0, 720.0)
            scene.fill = Color.TRANSPARENT
            scene.stylesheets.add(javaClass.getResource("/main2.css").toExternalForm())
//            scene.root.effect = DropShadow()
            stage.title = "King-lms Lite $version"
            stage.scene = scene
            stage.show()

            main.primaryStage = stage
            main.mainController = fxmlLoader.getController()
            main.changePopupMenu(true, Category.NEWS, Category.TASK)

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
