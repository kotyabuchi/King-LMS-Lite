package com.github.kabocchi.king_LMS_Lite.Controller

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.*
import com.github.kabocchi.king_LMS_Lite.Utility.decryptFile2
import com.github.kabocchi.king_LMS_Lite.Utility.encryptFile2
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
        if (!PROJECT_FOLDER.exists()) PROJECT_FOLDER.mkdirs()
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
            
            val loginResult = try {
                kcgLogin(idField.text, passField.text)
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
            idField.isDisable = false
            passField.isDisable = false
            loginButton.isDisable = false
        }
    }
    
    private fun kcgLogin(id: String, pass: String): LoginResult {
        try {
            HttpClientBuilder.create().setRedirectStrategy(LaxRedirectStrategy()).build().use { httpClient ->
                val config = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build()
                
                val formParams = mutableListOf<BasicNameValuePair>()
                formParams.add(BasicNameValuePair("__VIEWSTATE", "/wEPDwULLTE2MDkwMzkxOTRkZHY/AzvXjoMqTsVgJd4ipDEPUaNz"))
                formParams.add(BasicNameValuePair("__VIEWSTATEGENERATOR", "C57CFBF9"))
                formParams.add(BasicNameValuePair("__EVENTVALIDATION", "/wEdAASsWh7OxHZiOdC3v4rgI+lhmoSnNhet8R/Uqc0Y+L4tIt5lw99SYJ+Wv9EE4DvTk2BF8gstbfJCPOTeBk01E6UD2dD7i/ZD0yK6ahPUIO4y8Y1lnjI="))
                formParams.add(BasicNameValuePair("TextLoginID", id))
                formParams.add(BasicNameValuePair("TextPassword", pass))
                formParams.add(BasicNameValuePair("buttonHtmlLogon", "ログイン"))
                val entity = UrlEncodedFormEntity(formParams, "UTF-8")
        
                val httpPost = HttpPost("https://king.kcg.kyoto/campus/Secure/login.aspx")
    
                httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                httpPost.config = config
                httpPost.entity = entity
    
                httpClient.execute(httpPost, context).use { httpResponse ->
                    return if (httpResponse.statusLine.statusCode == HttpStatus.SC_OK) {
                        LoginResult.SUCCESS
                    } else {
                        LoginResult.FAIL
                    }
                }
            }
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
