package com.github.kabocchi.king_LMS_Lite.Controller

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Utility.decryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.encryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.loginToKINGLMS
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.apache.http.HttpStatus
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class Login {

    @FXML private lateinit var idField: TextField
    @FXML private lateinit var passField: PasswordField
    @FXML private lateinit var error: Label
    @FXML private lateinit var loginButton: Button
    
    @FXML
    fun initialize() {
        if (!PROJECT_FOLDER.exists()) PROJECT_FOLDER.mkdirs()
        if (ACCOUNT_FILE.exists()) {
            val json = Json.parse(decryptFile2(ACCOUNT_FILE_PATH)).asObject()
            idField.text = json.getString("id", "")
            passField.text = json.getString("pass", "")
            val lastLogin = json.getString("lastLogin", "")
            if (lastLogin != "" && ChronoUnit.WEEKS.between(LocalDateTime.now(), LocalDateTime.parse(lastLogin)) >= 1) {
                idField.text = ""
                passField.text = ""
                val newJson = JsonObject()
                newJson.add("id", "").add("pass", "").add("lastLogin", lastLogin)
                encryptFile2(ACCOUNT_FILE_PATH, newJson)
                showError("最終ログインから１週間が経過しました。\n再度IDとパスワードを入力してください。")
            } else {
                Thread(Runnable {
                    login()
                }).start()
            }
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

            var doMore = false
            var doCount = 0

            do {
                doCount++
                if (doMore) {
                    showError("ログインに失敗しました。再試行します(${doCount}回目)")
                }
                doMore = false
                val start = System.currentTimeMillis()
                try {
                    val loginResult = loginToKINGLMS(idField.text, passField.text)
                    Platform.runLater {
                        when (loginResult) {
                            LoginResult.SUCCESS -> {
                                error.isVisible = false
                                val json = JsonObject()
                                json.add("id", idField.text).add("pass", passField.text).add("lastLogin", LocalDateTime.now().toString())
                                encryptFile2(ACCOUNT_FILE_PATH, json)
                                println("ログインしました。")
                                main?.let {
                                    it.primaryStage?.close()
                                    it.appUtil.showMain(Stage())
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    doMore = true
                }
            } while (doMore && doCount < 5)

            idField.isDisable = false
            passField.isDisable = false
            loginButton.isDisable = false
        }
    }

    private fun showError(text: String) {
        Platform.runLater {
            error.text = text
            error.isVisible = true
        }
    }
}
