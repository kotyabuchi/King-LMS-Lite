package com.github.kabocchi.king_LMS_Lite.Node

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Setting.*
import com.github.kabocchi.king_LMS_Lite.Utility.decryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.encryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.getDocumentWithJsoup
import com.github.kabocchi.king_LMS_Lite.Utility.toMap
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import javafx.util.Duration


class SettingPane: BorderPane() {
    private val mapper = ObjectMapper()

    private lateinit var setting: Setting
    private lateinit var popupNotificationSetting: PopupNotificationSetting
    private lateinit var mailNotificationSetting: MailNotificationSetting
    private lateinit var newsSaveSetting: NewsSaveSetting
    private lateinit var taskSaveSetting: TaskSaveSetting

    
    
    private val newsSaveFolderPath: TextField
    private val newsSelectFilePath: Button
    private val newsAskingEachTime: CheckBox

    private val taskSaveFolderPath: TextField
    private val taskSelectFilePath: Button
    private val taskAskingEachTime: CheckBox
    private val taskSaveToGroupFolder: CheckBox
    
    private var notificationOpen = false
    private var notificationHeight = 0.0
    private var saveFileOpen = false
    private var saveFileHeight = 0.0

    init {
        val scrollPane = ScrollPane().apply {
            isPannable = true
            isFitToWidth = true
            prefWidthProperty().bind(this@SettingPane.widthProperty())
        }
        this.center = scrollPane
        
        val mainVBox = VBox().apply {
            style = "-fx-background-color: white;"
            padding = Insets(40.0)
            spacing = 10.0
            scrollPane.content = this
        }
        
        val notificationSettingLabelBox = BorderPane().apply {
            cursor = Cursor.HAND
            left = Label("通知設定").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            
        }
        
        val notificationSettingTab = AnchorPane().apply {
            minHeight = 0.0
            children.add(VBox().apply {
                spacing = 12.0
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
                val notificationPopupLabel = Label("ポップアップ通知")
                val notificationPopupContainer = HBox().apply {
                    spacing = 4.0
                    alignment = Pos.BOTTOM_LEFT
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
    
                val notificationMailLabel = Label("メール通知")
                val notificationMailContainer = HBox().apply {
                    spacing = 4.0
                    alignment = Pos.BOTTOM_LEFT
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
            })
        }
        Rectangle().apply {
            widthProperty().bind(notificationSettingTab.widthProperty())
            heightProperty().bind(notificationSettingTab.maxHeightProperty())
            notificationSettingTab.clip = this
        }
        notificationSettingTab.layoutBoundsProperty().addListener { _, _, bounds2 ->
            if (notificationHeight == 0.0 && bounds2.height > 0) {
                notificationHeight = bounds2.height
                val openAnim = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(notificationSettingTab.maxHeightProperty(), notificationHeight))).apply {
                    cycleCount = 1
                }
                val closeAnim = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(notificationSettingTab.maxHeightProperty(), 0.0))).apply {
                    cycleCount = 1
                }
                notificationSettingLabelBox.setOnMouseClicked {
                    if (notificationOpen) {
                        closeAnim.play()
                    } else {
                        openAnim.play()
                    }
                    notificationOpen = !notificationOpen
                }
                Platform.runLater {
                    notificationSettingTab.maxHeight = 0.0
                }
            }
        }

        val saveFileSettingLabelBox = BorderPane().apply {
            cursor = Cursor.HAND
            left = Label("ファイル保存設定").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
        }
        val saveFileSettingTab = AnchorPane().apply {
            minHeight = 0.0
            children.add(VBox().apply {
                spacing = 12.0
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
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
            })
        }
        Rectangle().apply {
            widthProperty().bind(saveFileSettingTab.widthProperty())
            heightProperty().bind(saveFileSettingTab.maxHeightProperty())
            saveFileSettingTab.clip = this
        }
        saveFileSettingTab.layoutBoundsProperty().addListener { _, _, bounds2 ->
            if (saveFileHeight == 0.0 && bounds2.height > 0) {
                saveFileHeight = bounds2.height
                val openAnim = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(saveFileSettingTab.maxHeightProperty(), saveFileHeight))).apply {
                    cycleCount = 1
                }
                val closeAnim = Timeline(KeyFrame(Duration.seconds(0.2), KeyValue(saveFileSettingTab.maxHeightProperty(), 0.0))).apply {
                    cycleCount = 1
                }
                saveFileSettingLabelBox.setOnMouseClicked {
                    if (saveFileOpen) {
                        closeAnim.play()
                    } else {
                        openAnim.play()
                    }
                    saveFileOpen = !saveFileOpen
                }
                Platform.runLater {
                    saveFileSettingTab.maxHeight = 0.0
                }
            }
        }
        
        mainVBox.children.addAll(notificationSettingLabelBox, notificationSettingTab, Separator(), saveFileSettingLabelBox, saveFileSettingTab, Separator())
        
        val bottomContainer = HBox().apply {
            style = "-fx-background-color: #fff;"
            spacing = 20.0
            alignment = Pos.CENTER_RIGHT
            padding = Insets(8.0)
        }
        Button("ログアウト").apply {
            styleClass.add("border-button")
            bottomContainer.children.add(this)
            setOnAction {
                getDocumentWithJsoup(context.cookieStore.toMap(), "https://king.kcg.kyoto/campus/Secure/Logoff.aspx")
                ACCOUNT_FILE.delete()
                main?.let {
                    it.primaryStage?.close()
                    it.appUtil.showLogin(Stage())
                }
            }
        }
        Button("保存").apply {
            styleClass.add("apply-button")
            bottomContainer.children.add(this)
            AnchorPane.setBottomAnchor(this, 0.0)
            AnchorPane.setRightAnchor(this, 0.0)
            setOnAction {
                saveSetting()
            }
        }
        this.bottom = bottomContainer
        
        loadSetting()
    }

    private fun loadSetting() {
        if (!PROJECT_FOLDER.exists()) PROJECT_FOLDER.mkdirs()
        setting = if (SETTING_FILE.exists()) {
            mapper.readValue(decryptFile2(SETTING_FILE_PATH), Setting::class.java)
        } else {
            Setting()
        }
        popupNotificationSetting = setting.notificationSetting.popupNotificationSetting
        mailNotificationSetting = setting.notificationSetting.mailNotificationSetting
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
