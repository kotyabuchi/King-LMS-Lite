package com.github.kabocchi.king_LMS_Lite.Node.MainPane

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Node.BorderButton
import com.github.kabocchi.king_LMS_Lite.Setting.*
import com.github.kabocchi.king_LMS_Lite.Setting.Notification.MailNotificationSetting
import com.github.kabocchi.king_LMS_Lite.Setting.Notification.PopupNotificationSetting
import com.github.kabocchi.king_LMS_Lite.Setting.SaveFile.NewsSaveSetting
import com.github.kabocchi.king_LMS_Lite.Setting.SaveFile.TaskSaveSetting
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
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.shape.Rectangle
import javafx.stage.DirectoryChooser
import javafx.util.Duration
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

object SettingPane: BorderPane() {
    private val mapper = ObjectMapper()

    private lateinit var setting: Setting
    private lateinit var popupNotificationSetting: PopupNotificationSetting
    private lateinit var mailNotificationSetting: MailNotificationSetting
    private lateinit var newsSaveSetting: NewsSaveSetting
    private lateinit var taskSaveSetting: TaskSaveSetting
    private lateinit var detailCacheSetting: DetailCacheSetting

    private val notificationTabMark: Label
    private val saveFileTabMark: Label
    private val cacheTabMark: Label
    private val themeTabMark: Label

    private val sendPopup: CheckBox
    private val popupDayText: ChoiceBox<String>

    private val popupTimeText: ChoiceBox<String>
    private val sendMail: CheckBox
    private val mailDayText: ChoiceBox<String>

    private val mailTimeText: ChoiceBox<String>
    private val newsSaveFolderPath: TextField
    private val newsSelectFilePath: Button

    private val newsAskingEachTime: CheckBox
    private val taskSaveFolderPath: TextField
    private val taskSelectFilePath: Button
    private val taskAskingEachTime: CheckBox
    private val taskSaveToGroupFolder: CheckBox

    private val newsDetailCache: CheckBox
    private val newsDetailCachePath: TextField
    private val newsDetailCache_selectFilePath: Button
    private val taskDetailCache: CheckBox
    private val taskDetailCachePath: TextField
    private val taskDetailCache_selectFilePath: Button

    private val progressText: Label
    private var progressTextRemoveTimer: TimerTask? = null
    
    private var notificationOpen = false
    private var notificationHeight = 0.0
    private var saveFileOpen = false
    private var saveFileHeight = 0.0
    private var detailCacheOpen = false
    private var detailCacheHeight = 0.0
    private var themeOpen = false
    private var themeHeight = 0.0

    init {
        this.styleClass.add("settin-pane")
        val scrollPane = ScrollPane().apply {
            isPannable = true
            isFitToWidth = true
            prefWidthProperty().bind(this@SettingPane.widthProperty())
        }
        this.center = scrollPane
        
        val mainVBox = VBox(10.0).apply {
//            style = "-fx-background-color: white;"
            padding = Insets(30.0, 20.0, 30.0, 20.0)
            scrollPane.content = this
        }

        // ===========================================================================================================
        val notificationSettingLabelBox = BorderPane().apply {
            cursor = Cursor.HAND
            padding = Insets(0.0, 20.0, 0.0, 10.0)
            left = Label("通知設定").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            notificationTabMark = Label("▼").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            right = notificationTabMark
        }
        
        val notificationSettingTab = AnchorPane().apply {
            minHeight = 0.0
            padding = Insets(0.0, 15.0, 0.0, 15.0)
            children.add(VBox(12.0).apply {
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
                val notificationPopupLabel = Label("ポップアップ通知")
                val notificationPopupContainer = HBox(4.0).apply {
                    val dayList = FXCollections.observableArrayList<String>()
                    for (i in 1..27) {
                        dayList.add(i.toString())
                    }
                    alignment = Pos.BOTTOM_LEFT
                    popupDayText =  ChoiceBox(dayList).apply {
                        minWidth = 60.0
                        selectionModel.select(4)
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
                    popupTimeText = ChoiceBox(timeList)
                    popupTimeText.selectionModel.select(18)
                    children.addAll(popupDayText, dayLabel, popupTimeText)
                }
                sendPopup = CheckBox("ポップアップ通知を利用する").apply {
                    isSelected = true
                    setOnAction {
                        popupDayText.isDisable = !isSelected
                        popupTimeText.isDisable = !isSelected
                    }
                }
    
                val notificationMailLabel = Label("メール通知")
                val notificationMailContainer = HBox(4.0).apply {
                    alignment = Pos.BOTTOM_LEFT
                    val dayList = FXCollections.observableArrayList<String>()
                    for (i in 1..27) {
                        dayList.add(i.toString())
                    }
                    alignment = Pos.BOTTOM_LEFT
                    mailDayText =  ChoiceBox(dayList).apply {
                        minWidth = 60.0
                        selectionModel.select(4)
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
                    mailTimeText = ChoiceBox(timeList)
                    mailTimeText.selectionModel.select(18)
                    children.addAll(mailDayText, dayLabel, mailTimeText)
                }
                sendMail = CheckBox("メール通知を利用する").apply {
                    isSelected = true
                    setOnAction {
                        mailDayText.isDisable = !isSelected
                        mailTimeText.isDisable = !isSelected
                    }
                }
                children.addAll(notificationPopupLabel, sendPopup, notificationPopupContainer, notificationMailLabel, sendMail, notificationMailContainer)
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
                val openAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(notificationSettingTab.maxHeightProperty(), notificationHeight)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(notificationTabMark.rotateProperty(), 180.0))).apply {
                    cycleCount = 1
                }
                val closeAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(notificationSettingTab.maxHeightProperty(), 0.0)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(notificationTabMark.rotateProperty(), 0.0))).apply {
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
        // ===========================================================================================================
        val saveFileSettingLabelBox = BorderPane().apply {
            cursor = Cursor.HAND
            padding = Insets(0.0, 20.0, 0.0, 10.0)
            left = Label("ファイル保存設定").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            saveFileTabMark = Label("▼").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            right = saveFileTabMark
        }
        val saveFileSettingTab = AnchorPane().apply {
            minHeight = 0.0
            padding = Insets(0.0, 15.0, 0.0, 15.0)
            children.add(VBox(12.0).apply {
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
                val newsSection = VBox(10.0).apply {
                    val newsSectionLabel = Label("お知らせ")
                    newsSaveFolderPath = TextField().apply {
                        minWidth = 600.0
                    }
                    newsSelectFilePath = Button("フォルダを選択").apply {
                        styleClass.add("border-button")
                        setOnAction {
                            val directoryChooser = DirectoryChooser()
                            directoryChooser.initialDirectory = File(System.getProperty("user.home"))
                            val choosePath = directoryChooser.showDialog(null) ?: return@setOnAction
                            newsSaveFolderPath.text = choosePath.path + File.separator
                        }
                    }
                    newsAskingEachTime = CheckBox("ファイルごとに保存先を指定する").apply {
                        setOnAction {
                            newsSelectFilePath.isDisable = isSelected
                            newsSaveFolderPath.isDisable = isSelected
                        }
                    }
                    children.addAll(newsSectionLabel, newsAskingEachTime, HBox(newsSaveFolderPath, newsSelectFilePath))
                }
                val taskSection = VBox(10.0).apply {
                    val taskSectionLabel = Label("課題")
                    taskSaveFolderPath = TextField().apply {
                        minWidth = 600.0
                    }
                    taskSelectFilePath = Button("フォルダを選択").apply {
                        styleClass.add("border-button")
                        setOnAction {
                            val directoryChooser = DirectoryChooser()
                            directoryChooser.initialDirectory = File(System.getProperty("user.home"))
                            val choosePath = directoryChooser.showDialog(null) ?: return@setOnAction
                            newsSaveFolderPath.text = choosePath.path + File.separator
                        }
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
                var animating = false
                val openAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(saveFileSettingTab.maxHeightProperty(), saveFileHeight)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(saveFileTabMark.rotateProperty(), 180.0))).apply {
                    cycleCount = 1
                    setOnFinished {
                        animating = false
                    }
                }
                val closeAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(saveFileSettingTab.maxHeightProperty(), 0.0)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(saveFileTabMark.rotateProperty(), 0.0))).apply {
                    cycleCount = 1
                    setOnFinished {
                        animating = false
                    }
                }
                saveFileSettingLabelBox.setOnMouseClicked {
                    if (animating) return@setOnMouseClicked
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

        // ===========================================================================================================
        val detailCacheSettingLabelBox = BorderPane().apply {
            cursor = Cursor.HAND
            padding = Insets(0.0, 20.0, 0.0, 10.0)
            left = Label("詳細データキャッシュ設定").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            cacheTabMark = Label("▼").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            right = cacheTabMark
        }
        val detailCacheSettingTab = AnchorPane().apply {
            minHeight = 0.0
            padding = Insets(0.0, 15.0, 0.0, 15.0)
            children.add(VBox(12.0).apply {
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
                newsDetailCachePath = TextField().apply {
                    minWidth = 600.0
                }
                newsDetailCache_selectFilePath = Button("フォルダを選択").apply {
                    styleClass.add("border-button")
                    setOnAction {
                        val directoryChooser = DirectoryChooser()
                        directoryChooser.initialDirectory = File(System.getProperty("user.home"))
                        val choosePath = directoryChooser.showDialog(null) ?: return@setOnAction
                        newsDetailCachePath.text = choosePath.path + File.separator
                    }
                }
                newsDetailCache = CheckBox("お知らせの詳細をキャッシュする").apply {
                    setOnAction {
                        newsDetailCachePath.isDisable = !isSelected
                        newsDetailCache_selectFilePath.isDisable = !isSelected
                    }
                }
                taskDetailCachePath = TextField().apply {
                    minWidth = 600.0
                }
                taskDetailCache_selectFilePath = Button("フォルダを選択").apply {
                    styleClass.add("border-button")
                    setOnAction {
                        val directoryChooser = DirectoryChooser()
                        directoryChooser.initialDirectory = File(System.getProperty("user.home"))
                        val choosePath = directoryChooser.showDialog(null) ?: return@setOnAction
                        taskDetailCachePath.text = choosePath.path + File.separator
                    }
                }
                taskDetailCache = CheckBox("課題の詳細をキャッシュする").apply {
                    setOnAction {
                        taskDetailCachePath.isDisable = !isSelected
                        taskDetailCache_selectFilePath.isDisable = !isSelected
                    }
                }
                children.addAll(newsDetailCache, HBox(newsDetailCachePath, newsDetailCache_selectFilePath), taskDetailCache, HBox(taskDetailCachePath, taskDetailCache_selectFilePath))
            })
        }
        Rectangle().apply {
            widthProperty().bind(detailCacheSettingTab.widthProperty())
            heightProperty().bind(detailCacheSettingTab.maxHeightProperty())
            detailCacheSettingTab.clip = this
        }
        detailCacheSettingTab.layoutBoundsProperty().addListener { _, _, bounds2 ->
            if (detailCacheHeight == 0.0 && bounds2.height > 0) {
                detailCacheHeight = bounds2.height
                val openAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(detailCacheSettingTab.maxHeightProperty(), detailCacheHeight)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(cacheTabMark.rotateProperty(), 180.0))).apply {
                    cycleCount = 1
                }
                val closeAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(detailCacheSettingTab.maxHeightProperty(), 0.0)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(cacheTabMark.rotateProperty(), 0.0))).apply {
                    cycleCount = 1
                }
                detailCacheSettingLabelBox.setOnMouseClicked {
                    if (detailCacheOpen) {
                        closeAnim.play()
                    } else {
                        openAnim.play()
                    }
                    detailCacheOpen = !detailCacheOpen
                }
                Platform.runLater {
                    detailCacheSettingTab.maxHeight = 0.0
                }
            }
        }
        // ===========================================================================================================
        val themeSettingLabelBox = BorderPane().apply {
            cursor = Cursor.HAND
            padding = Insets(0.0, 20.0, 0.0, 10.0)
            left = Label("テーマ設定").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            themeTabMark = Label("▼").apply {
                style = "-fx-font-weight: bold;"
                styleClass.add("h1")
            }
            right = themeTabMark
        }
        val themeSettingTab = AnchorPane().apply {
            minHeight = 0.0
            padding = Insets(0.0, 15.0, 0.0, 15.0)
            children.add(VBox(12.0).apply {
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
                val templatesContainer = FlowPane().apply {
                    vgap = 6.0
                    hgap = 10.0
                    val radioButton = RadioButton().apply {
                        width = 80.0
                        height = 60.0
                        style = """
                            -fx-background-color: #F89174, white, rgb(30, 30, 30), #e74c3c;
                            -fx-background-insets: 0 0 0 0, 0 0 0 20px, 0 0 0 40px, 0 0 0 60px;
                        """.trimIndent()
                    }
                    val colorContainer = VBox(0.0).apply {
                        val mainColor = Label().apply {
                            width = 80.0
                            height = 20.0
                            style = "-fx-background-color: #F89174;"
                        }
                    }
                    children.addAll(radioButton, colorContainer)
                }
                children.addAll(templatesContainer)
            })
        }
        Rectangle().apply {
            widthProperty().bind(themeSettingTab.widthProperty())
            heightProperty().bind(themeSettingTab.maxHeightProperty())
            themeSettingTab.clip = this
        }
        themeSettingTab.layoutBoundsProperty().addListener { _, _, bounds2 ->
            if (themeHeight == 0.0 && bounds2.height > 0) {
                themeHeight = bounds2.height
                val openAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(themeSettingTab.maxHeightProperty(), themeHeight)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(themeTabMark.rotateProperty(), 180.0))).apply {
                    cycleCount = 1
                }
                val closeAnim = Timeline(
                        KeyFrame(Duration.seconds(0.2), KeyValue(themeSettingTab.maxHeightProperty(), 0.0)),
                        KeyFrame(Duration.seconds(0.2), KeyValue(themeTabMark.rotateProperty(), 0.0))).apply {
                    cycleCount = 1
                }
                themeSettingLabelBox.setOnMouseClicked {
                    if (themeOpen) {
                        closeAnim.play()
                    } else {
                        openAnim.play()
                    }
                    themeOpen = !themeOpen
                }
                Platform.runLater {
                    themeSettingTab.maxHeight = 0.0
                }
            }
        }


        mainVBox.children.addAll(
                notificationSettingLabelBox, notificationSettingTab, Separator(),
                saveFileSettingLabelBox, saveFileSettingTab, Separator(),
                detailCacheSettingLabelBox, detailCacheSettingTab, Separator(),
                themeSettingLabelBox, themeSettingTab, Separator())
        
        val bottomContainer = HBox(10.0).apply {
            style = "-fx-background-color: transparent;"
            alignment = Pos.CENTER_RIGHT
            padding = Insets(8.0, 10.0, 8.0, 10.0)
        }
        progressText = Label().apply {
            style = "-fx-font-weight: bold; -fx-font-size: 14px;"
            bottomContainer.children.add(this)
        }
        BorderButton("ログアウト").apply {
//            styleClass.add("border-button")
            bottomContainer.children.add(this)
            setOnAction {
                getDocumentWithJsoup(context.cookieStore.toMap(), "https://king.kcg.kyoto/campus/Secure/Logoff.aspx")
//                ACCOUNT_FILE.delete()
                context = HttpClientContext.create()
                cookieStore = BasicCookieStore()
                context.cookieStore = cookieStore
//                main?.let {
//                    it.primaryStage?.close()
//                    it.appUtil.showLogin(Stage())
//                }
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
        detailCacheSetting = setting.detailCacheSetting

        sendPopup.isSelected = popupNotificationSetting.send ?: true
        popupDayText.selectionModel.select(popupNotificationSetting.day ?: "3")
        popupTimeText.selectionModel.select(popupNotificationSetting.time ?: "午前 09:00")

        sendMail.isSelected = mailNotificationSetting.send ?: true
        mailDayText.selectionModel.select(mailNotificationSetting.day ?: "3")
        mailTimeText.selectionModel.select(mailNotificationSetting.time ?: "午前 09:00")

        newsSaveFolderPath.text = newsSaveSetting.folderPath ?: "news/"
        newsSaveFolderPath.isDisable = newsSaveSetting.askingEachTime ?: true
        newsSelectFilePath.isDisable = newsSaveSetting.askingEachTime ?: true
        newsAskingEachTime.isSelected = newsSaveSetting.askingEachTime ?: true

        taskSaveFolderPath.text = taskSaveSetting.folderPath ?: "task/"
        taskSaveFolderPath.isDisable = taskSaveSetting.askingEachTime ?: true
        taskSelectFilePath.isDisable = taskSaveSetting.askingEachTime ?: true
        taskAskingEachTime.isSelected = taskSaveSetting.askingEachTime ?: true
        taskSaveToGroupFolder.isSelected = taskSaveSetting.saveToGroupFolder ?: false

        newsDetailCache.isSelected = detailCacheSetting.newsCache ?: false
        taskDetailCache.isSelected = detailCacheSetting.taskCache ?: false
        newsDetailCachePath.text = detailCacheSetting.newsPath ?: "newsCache/"
        taskDetailCachePath.text = detailCacheSetting.taskPath ?: "taskCache/"
        newsDetailCachePath.isDisable = !(detailCacheSetting.newsCache ?: false)
        newsDetailCache_selectFilePath.isDisable = !(detailCacheSetting.newsCache ?: false)
        taskDetailCachePath.isDisable = !(detailCacheSetting.taskCache ?: false)
        taskDetailCache_selectFilePath.isDisable = !(detailCacheSetting.taskCache ?: false)
    }

    private fun saveSetting() {
        try {
            popupNotificationSetting.apply {
                send = sendPopup.isSelected
                day = popupDayText.value
                time = popupTimeText.value
            }

            mailNotificationSetting.apply {
                send = sendMail.isSelected
                day = mailDayText.value
                time = mailTimeText.value
            }

            newsSaveSetting.apply {
                folderPath = newsSaveFolderPath.text
                askingEachTime = newsAskingEachTime.isSelected
            }

            taskSaveSetting.apply {
                folderPath = taskSaveFolderPath.text
                askingEachTime = taskAskingEachTime.isSelected
                saveToGroupFolder = taskSaveToGroupFolder.isSelected
            }

            detailCacheSetting.apply {
                newsCache = newsDetailCache.isSelected
                taskCache = taskDetailCache.isSelected
                newsPath = newsDetailCachePath.text
                taskPath = taskDetailCachePath.text
            }
            encryptFile2(SETTING_FILE_PATH, mapper.writeValueAsString(setting))
            changeProgressText("設定を保存しました")
        } catch (e: Exception) {
            changeProgressText("設定の保存に失敗しました")
        }
    }

    private fun changeProgressText(text: String, rewriteToBlank: Boolean = true) {
        Platform.runLater {
            progressText.text = text
        }
        if (rewriteToBlank) {
            progressTextRemoveTimer?.cancel()
            val timer = Timer()
            progressTextRemoveTimer = timerTask {
                Platform.runLater {
                    progressText.text = ""
                }
                progressTextRemoveTimer = null
            }
            timer.schedule(progressTextRemoveTimer, 3000)
        }
    }

    fun getPopupNotificationSetting(): PopupNotificationSetting {
        return popupNotificationSetting
    }

    fun getMailNotificationSetting(): MailNotificationSetting {
        return mailNotificationSetting
    }

    fun getNewsSaveSetting(): NewsSaveSetting {
        return newsSaveSetting
    }

    fun getTaskSaveSetting(): TaskSaveSetting {
        return taskSaveSetting
    }
}
