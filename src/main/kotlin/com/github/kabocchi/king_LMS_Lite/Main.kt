package com.github.kabocchi.king_LMS_Lite

import com.github.kabocchi.king_LMS_Lite.Controller.MainController
import com.github.kabocchi.king_LMS_Lite.Utility.AppUtil
import com.github.kabocchi.king_LMS_Lite.Utility.newLoginTest
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import kotlin.system.exitProcess

val FOLDER_PATH = System.getenv("APPDATA") + File.separator + "King-lms Lite" + File.separator
val ACCOUNT_FILE_PATH = FOLDER_PATH + "account.klite"
val EVENTS_FILE_PATH = FOLDER_PATH + "events.klite"
val SETTING_FILE_PATH = FOLDER_PATH + "setting.klite"
val PROJECT_FODLER = File(FOLDER_PATH)
val ACCOUNT_FILE = File(ACCOUNT_FILE_PATH)
var connection: Connection = Jsoup.connect("https://king.kcg.kyoto/campus/Secure/Login.aspx?ReturnUrl=%2Fcampus%2FCommunity%2FMySetting")
val os = System.getProperty("os.name").toLowerCase()
var main: Main? = null

fun main(args: Array<String>) {
//    Application.launch(Main::class.java, *args)
    newLoginTest()
}

class Main: Application() {

    private val VERSION = "v0.1.0"
    val appUtil: AppUtil

    var primaryStage: Stage? = null
    var mainController: MainController? = null
    private val systemTrayImage = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("logo.png"))
    private val trayIcon = TrayIcon(systemTrayImage, "King-lms Lite $VERSION")

    init {
        appUtil = AppUtil(VERSION, this)
    }

    override fun start(stage: Stage?) {
        main = this
        if (stage != null) {
            primaryStage = stage
            Platform.setImplicitExit(false)
            stage.icons.add(Image(ClassLoader.getSystemResourceAsStream("logo.png")))
            initSystemTray()
            appUtil.showLogin(stage)
        }
    }

    private fun initSystemTray() {
        if (!SystemTray.isSupported()) return

        val systemTray = SystemTray.getSystemTray()

        val updateNewsMenu = MenuItem("お知らせを更新する").apply {
            addActionListener {
                mainController?.getNews()
            }
            isEnabled = false
        }

        val updateTaskMenu = MenuItem("課題を更新する").apply {
            addActionListener {
                mainController?.getTask()
            }
            isEnabled = false
        }

        val exitMenu = MenuItem("終了").apply {
            addActionListener {
                exit()
            }
        }

        val popupMenu = PopupMenu().apply {
            add(updateNewsMenu)
            add(updateTaskMenu)
            addSeparator()
            add(exitMenu)
        }

        trayIcon.apply {
            isImageAutoSize = true
            setPopupMenu(popupMenu)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.button == MouseEvent.BUTTON1) {
                        if (primaryStage == null) return
                        if (primaryStage!!.isShowing) return
                        Platform.runLater{
                            primaryStage!!.show()
                        }
                    }
                }
            })
        }

        try  {
            systemTray.add(trayIcon)
        } catch (e: AWTException) {
            e.printStackTrace()
        }
    }

    fun getVersion(): String {
        return VERSION
    }

    fun exit() {
        SystemTray.getSystemTray().remove(trayIcon)
        Platform.exit()
        exitProcess(0)
    }
}
