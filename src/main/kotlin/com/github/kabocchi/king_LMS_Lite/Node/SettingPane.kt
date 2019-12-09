package com.github.kabocchi.king_LMS_Lite.Node

import com.github.kabocchi.king_LMS_Lite.ACCOUNT_FILE
import com.github.kabocchi.king_LMS_Lite.Utility.getDocument
import com.github.kabocchi.king_LMS_Lite.connection
import com.github.kabocchi.king_LMS_Lite.main
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

class SettingPane: VBox() {

    init {
        this.apply {
            style = "-fx-background-color: white;"
            padding = Insets(20.0)
            spacing = 20.0
        }

        val logoutBorder = BorderPane()
        val logoutButton = Button("ログアウト").apply {
            styleClass.add("border-button")
            logoutBorder.center = this
            setOnAction {
                getDocument(connection, "https://king.kcg.kyoto/campus/Secure/Logoff.aspx")
                ACCOUNT_FILE.delete()
                main?.let {
                    it.primaryStage?.close()
                    it.appUtil.showLogin(Stage())
                }
            }
        }
        this.children.addAll(logoutBorder)
    }
}