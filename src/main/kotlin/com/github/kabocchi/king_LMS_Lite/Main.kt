package com.github.kabocchi.king_LMS_Lite

import com.github.kabocchi.king_LMS_Lite.Controller.MainController
import com.github.kabocchi.king_LMS_Lite.Utility.AppUtil
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.time.LocalDateTime
import kotlin.system.exitProcess

val FOLDER_PATH = System.getenv("APPDATA") + File.separator + "King-lms Lite" + File.separator
val ACCOUNT_FILE_PATH = FOLDER_PATH + "account.klite"
val SETTING_FILE_PATH = FOLDER_PATH + "setting.klite"
val COLOR_SETTING_FILE_PATH = FOLDER_PATH + "color.klite"
val PROJECT_FOLDER = File(FOLDER_PATH)
val ACCOUNT_FILE = File(ACCOUNT_FILE_PATH)
val SETTING_FILE = File(SETTING_FILE_PATH)
val COLOR_SETTING_FILE = File(COLOR_SETTING_FILE_PATH)
var connection: Connection = Jsoup.connect("https://king.kcg.kyoto/campus/Secure/Login.aspx?ReturnUrl=%2Fcampus%2FCommunity%2FMySetting")
var context = HttpClientContext.create()
var cookieStore = BasicCookieStore()
val os = System.getProperty("os.name").toLowerCase()
var main: Main? = null

fun main(args: Array<String>) {
    context.cookieStore = cookieStore
    Application.launch(Main::class.java, *args)
}

class Main: Application() {

    private val VERSION = "v1.0.5"
    val appUtil: AppUtil

    var primaryStage: Stage? = null
    var mainController: MainController? = null

    private val systemTrayImage = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("logo.png"))
    private val trayIcon = TrayIcon(systemTrayImage, "KING-LMS Lite $VERSION")
    private lateinit var updateNewsMenu: MenuItem
    private lateinit var updateTaskMenu: MenuItem

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

        updateNewsMenu = MenuItem("お知らせを更新する").apply {
            addActionListener {
                mainController?.getNews()
            }
            isEnabled = false
        }

        updateTaskMenu = MenuItem("課題を更新する").apply {
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

    fun changePopupMenu(change: Boolean, vararg categories: Category) {
        if (categories.contains(Category.NEWS)) updateNewsMenu.isEnabled = change
        if (categories.contains(Category.TASK)) updateTaskMenu.isEnabled = change
    }
}
