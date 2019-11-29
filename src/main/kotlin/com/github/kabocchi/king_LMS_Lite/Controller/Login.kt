package com.github.kabocchi.king_LMS_Lite.Controller

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Utility.AppUtil
import com.github.kabocchi.king_LMS_Lite.Utility.decryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.encryptFile2
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class Login {

    @FXML private lateinit var idField: TextField
    @FXML private lateinit var passField: PasswordField
    @FXML private lateinit var error: Label
    @FXML private lateinit var loginButton: Button
    
    @FXML
    fun initialize() {
        if (!PROJECT_FODLER.exists()) PROJECT_FODLER.mkdirs()
        if (ACCOUNT_FILE.exists()) {
            val json = Json.parse(decryptFile2(ACCOUNT_FILE_PATH)).asObject()
            idField.text = json.getString("id", "")
            passField.text = json.getString("pass", "")
            Thread(Runnable {
                login()
            }).start()
        }
    }
    
    @FXML
    fun clickLoginButton() {
        Thread(Runnable {
            login()
        }).start()
    }

    @FXML
    fun enterId() {
        Thread(Runnable {
            login()
        }).start()
    }

    @FXML
    fun enterPass() {
        Thread(Runnable {
            login()
        }).start()
    }

    private fun login() {
        if (idField.text != "" && passField.text != "") {
            idField.isDisable = true
            passField.isDisable = true
            loginButton.isDisable = true
            error.isVisible = false
            val start = System.currentTimeMillis()
            connection = Jsoup.connect("https://king.kcg.kyoto/campus/Secure/Login.aspx?ReturnUrl=%2Fcampus%2FCommunity%2FMySetting")
            val loginResult = try {
                kcgLogin(connection, idField.text, passField.text)
            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException, is SocketTimeoutException, is SSLHandshakeException -> {
                        showError("ホストに接続できませんでした。ネットワークの接続を確認してください。")
                    }
                    else -> {
                        showError("エラーが発生しました")
                    }
                }
            }
            Platform.runLater {
                when (loginResult) {
                    LoginResult.SUCCESS -> {
                        error.isVisible = false
                        val json = JsonObject()
                        json.add("id", idField.text).add("pass", passField.text)
                        encryptFile2(ACCOUNT_FILE_PATH, json)
                        println("ログインしました。")
                        if (main != null) {
                            val version = main!!.getVersion()
                            main!!.primaryStage?.close()
                            val stage = Stage()
                            AppUtil(version, main!!).showMain(stage)
                        }
                    }
                    LoginResult.FAIL -> {
                        println("ログインに失敗しました。$loginResult")
                        error.text = "ログインに失敗しました"
                        error.isVisible = true
                    }
                    LoginResult.ERROR -> {
//                        println("エラーが発生しました。$loginResult")
//                        error.text = "エラーが発生しました"
//                        error.isVisible = true
                    }
                }
            }
            val end = System.currentTimeMillis()
            println("Login: " + (end - start).toString() + "ms")
            idField.isDisable = false
            passField.isDisable = false
            loginButton.isDisable = false
        }
    }
    
    private fun kcgLogin(conn: Connection, id: String, pass: String): LoginResult {
        try {
            val doc = conn.data("__VIEWSTATE", "/wEPDwULLTE2MDkwMzkxOTRkZHY/AzvXjoMqTsVgJd4ipDEPUaNz")
                    .data("__VIEWSTATEGENERATOR", "C57CFBF9")
                    .data("__EVENTVALIDATION", "/wEdAASsWh7OxHZiOdC3v4rgI+lhmoSnNhet8R/Uqc0Y+L4tIt5lw99SYJ+Wv9EE4DvTk2BF8gstbfJCPOTeBk01E6UD2dD7i/ZD0yK6ahPUIO4y8Y1lnjI=")
                    .data("TextLoginID", id)
                    .data("TextPassword", pass)
                    .data("buttonHtmlLogon", "ログイン")
                    .userAgent("Mozilla")
                    .timeout(10000)
                    .post()
            return if (doc != null && !doc.select("span#lblWarning").hasText()) {
                LoginResult.SUCCESS
            } else {
                LoginResult.FAIL
            }
        } catch (e: UnknownHostException) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw e
        } catch (e: Exception) {
            throw e
//                if (useSchoolWifi() && wifiLogin(id, pass)) {
//                    TimeUnit.SECONDS.sleep(3)
//                    loginCount++
//                } else {
//                    break
//                }
        }
    }

    private fun showError(text: String) {
        Platform.runLater {
            error.text = text
            error.isVisible = true
        }
    }
    
    enum class LoginResult {
        SUCCESS,
        FAIL,
        ERROR
    }
}
