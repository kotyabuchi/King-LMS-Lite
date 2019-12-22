package com.github.kabocchi.king_LMS_Lite.Node

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Setting.NewsSaveSetting
import com.github.kabocchi.king_LMS_Lite.Setting.Setting
import com.github.kabocchi.king_LMS_Lite.Setting.TaskSaveSetting
import com.github.kabocchi.king_LMS_Lite.Utility.decryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.encryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.getDocumentWithJsoup
import com.github.kabocchi.king_LMS_Lite.Utility.toMap
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage


class SettingPane: VBox() {
    private val mapper = ObjectMapper()

    private lateinit var setting: Setting
    private lateinit var newsSaveSetting: NewsSaveSetting
    private lateinit var taskSaveSetting: TaskSaveSetting

    private val newsSaveFolderPath: TextField
    private val newsSelectFilePath: Button
    private val newsAskingEachTime: CheckBox

    private val taskSaveFolderPath: TextField
    private val taskSelectFilePath: Button
    private val taskAskingEachTime: CheckBox
    private val taskSaveToGroupFolder: CheckBox

    init {
        this.apply {
            style = "-fx-background-color: white;"
            padding = Insets(40.0)
            spacing = 20.0
        }

        val notificationSettingLabel = Label("通知設定").apply {
            styleClass.add("h1")
        }
        val notificationSettingTab = VBox().apply {
            spacing = 16.0
            val notificationPopupLabel = Label("ポップアップ通知").apply {
                styleClass.add("h2")
            }
            val notificationPopupContainer = HBox().apply {
                spacing = 4.0
                val dayText = TextField().apply {
                    prefWidth = 40.0
                }
                val dayLabel = Label("日前の")
                val timeList = FXCollections.observableArrayList<String>()
                for (i in 0..11) {
                    val time = if (i < 10) "0$i" else "$i"
                    timeList.addAll("午前 $time:00", "午前 $time:30")
                }
                for (i in 0..11) {
                    val time = if (i < 10) "0$i" else "$i"
                    timeList.addAll("午後 $time:00", "午後 $time:30")
                }
                val timeText = ChoiceBox(timeList)
                timeText.selectionModel.select(18)
                children.addAll(dayText, dayLabel, timeText)
            }

            val notificationMailLabel = Label("メール通知")
            val notificationMailContainer = HBox().apply {
                spacing = 4.0
                val dayText = TextField().apply {
                    prefWidth = 64.0
                }
                val dayLabel = Label("日前の")
                val timeList = FXCollections.observableArrayList<String>()
                for (i in 0..11) {
                    val time = if (i < 10) "0$i" else "$i"
                    timeList.addAll("午前 $time:00", "午前 $time:30")
                }
                for (i in 0..11) {
                    val time = if (i < 10) "0$i" else "$i"
                    timeList.addAll("午後 $time:00", "午後 $time:30")
                }
                val timeText = ChoiceBox(timeList)
                timeText.selectionModel.select(18)
                children.addAll(dayText, dayLabel, timeText)
            }
            children.addAll(notificationPopupLabel, notificationPopupContainer, notificationMailLabel, notificationMailContainer)
        }

        val saveFileSettingLabel = Label("ファイル保存設定").apply {
            style = "-fx-font-size: 16px; "
        }
        val saveFileSettingTab = VBox().apply {
            spacing = 16.0
            val newsSection = VBox().apply {
                spacing = 10.0
                val newsSectionLabel = Label("お知らせ")
                newsSaveFolderPath = TextField().apply {
                    minWidth = 600.0
                }
                newsSelectFilePath = Button("フォルダを選択").apply {
                    styleClass.add("border-button")
                }
                newsAskingEachTime = CheckBox("ファイルごとに保存先を指定する").apply {
                    setOnAction {
                        newsSelectFilePath.isDisable = isSelected
                        newsSaveFolderPath.isDisable = isSelected
                    }
                }
                children.addAll(newsSectionLabel, newsAskingEachTime, HBox(newsSaveFolderPath, newsSelectFilePath))
            }
            val taskSection = VBox().apply {
                spacing = 10.0
                val taskSectionLabel = Label("課題")
                taskSaveFolderPath = TextField().apply {
                    minWidth = 600.0
                }
                taskSelectFilePath = Button("フォルダを選択").apply {
                    styleClass.add("border-button")
                }
                taskAskingEachTime = CheckBox("ファイルごとに保存先を指定する")
                taskSaveToGroupFolder = CheckBox("講義ごとにフォルダを生成し、保存する")
                taskAskingEachTime.setOnAction {
                    taskSelectFilePath.isDisable = taskAskingEachTime.isSelected
                    taskSaveFolderPath.isDisable = taskAskingEachTime.isSelected
                    taskSaveToGroupFolder.isSelected = false
                }
                taskSaveToGroupFolder.setOnAction {
                    taskSelectFilePath.isDisable = false
                    taskSaveFolderPath.isDisable = false
                    taskAskingEachTime.isSelected = false
                }
                children.addAll(taskSectionLabel, taskAskingEachTime, taskSaveToGroupFolder, HBox(taskSaveFolderPath, taskSelectFilePath))
            }
            children.addAll(newsSection, taskSection)
        }

        val logoutBorder = BorderPane()
        Button("ログアウト").apply {
            styleClass.add("border-button")
            logoutBorder.center = this
            setOnAction {
                getDocumentWithJsoup(context.cookieStore.toMap(), "https://king.kcg.kyoto/campus/Secure/Logoff.aspx")
                ACCOUNT_FILE.delete()
                main?.let {
                    it.primaryStage?.close()
                    it.appUtil.showLogin(Stage())
                }
            }
        }
        val bottomAnchorPane = AnchorPane()
        Button("保存").apply {
            styleClass.add("apply-button")
            bottomAnchorPane.children.add(this)
            AnchorPane.setBottomAnchor(this, 0.0)
            AnchorPane.setRightAnchor(this, 0.0)
            setOnAction {
                saveSetting()
            }
        }

        this.children.addAll(notificationSettingLabel, notificationSettingTab, Separator(), saveFileSettingLabel, saveFileSettingTab, Separator(), logoutBorder, bottomAnchorPane)

        loadSetting()
    }

    private fun loadSetting() {
        if (!PROJECT_FOLDER.exists()) PROJECT_FOLDER.mkdirs()
        setting = if (SETTING_FILE.exists()) {
            mapper.readValue(decryptFile2(SETTING_FILE_PATH), Setting::class.java)
        } else {
            Setting()
        }
        newsSaveSetting = setting.saveFileSetting.newsSaveSetting
        taskSaveSetting = setting.saveFileSetting.taskSaveSetting

        newsSaveFolderPath.text = newsSaveSetting.folderPath ?: "news/"
        newsSaveFolderPath.isDisable = newsSaveSetting.askingEachTime ?: true
        newsSelectFilePath.isDisable = newsSaveSetting.askingEachTime ?: true
        newsAskingEachTime.isSelected = newsSaveSetting.askingEachTime ?: true

        taskSaveFolderPath.text = taskSaveSetting.folderPath ?: "task/"
        taskSaveFolderPath.isDisable = taskSaveSetting.askingEachTime ?: true
        taskSelectFilePath.isDisable = taskSaveSetting.askingEachTime ?: true
        taskAskingEachTime.isSelected = taskSaveSetting.askingEachTime ?: true
        taskSaveToGroupFolder.isSelected = taskSaveSetting.saveToGroupFolder ?: false
    }

    private fun saveSetting() {
        newsSaveSetting.apply {
            folderPath = newsSaveFolderPath.text
            askingEachTime = newsAskingEachTime.isSelected
        }

        taskSaveSetting.apply {
            folderPath = taskSaveFolderPath.text
            askingEachTime = taskAskingEachTime.isSelected
            saveToGroupFolder = taskSaveToGroupFolder.isSelected
        }
        encryptFile2(SETTING_FILE_PATH, mapper.writeValueAsString(setting))
    }
}
