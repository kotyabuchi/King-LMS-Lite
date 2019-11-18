package com.github.kabocchi.king_LMS_Lite.Utility

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.kingLmsLite.Node.NewsContent
import com.github.kabocchi.king_LMS_Lite.Style
import com.github.kabocchi.king_LMS_Lite.Task
import com.github.kabocchi.king_LMS_Lite.connection
import javafx.scene.Node
import javafx.scene.control.Hyperlink
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.Text
import java.awt.Desktop
import java.net.URI
import java.time.format.DateTimeFormatter
import kotlin.math.ceil


fun createNewsDetail(json: JsonObject, description: String): VBox {
    val description = description
    val date = json.getString("PublishedFrom", "")
    val newsId = json.getInt("Id", 0).toString()
//    val documents = json.get("Files").asArray()

    val contens = mutableListOf<Node>()

    val textArea = TextArea()
    textArea.text = description
    textArea.font = Font(14.0)
    textArea.minHeight = 150.0
    textArea.isEditable = false
    textArea.isWrapText = true
    contens.add(textArea)

//    if (documents.size() > 0) {
//        val documentText = Text("添付資料: ")
//        val documentBox = VBox(0.0, documentText)
//        for (document in documents) {
//            val documentJson = document.asObject()
//            val fileName = documentJson.getString("FileName", "")
//            val documentButton = JFXButton(fileName)
//            documentButton.textFill = Color.web("#5a92f1")
//            val documentIdText = Text(documentJson.getInt("Id", 0).toString())
//            documentIdText.isVisible = false
//            val detailBox = HBox(0.0, documentButton, documentIdText)
//            documentBox.children.add(detailBox)
//
//            documentButton.onAction = EventHandler<ActionEvent> {
//                val extension = fileName.split(Regex("\\.")).last()
//                val fileChooser = FileChooser()
//                val mainFilter = FileChooser.ExtensionFilter(extension.toUpperCase() + " files (*.$extension)", "*.$extension")
//                val subFilter = FileChooser.ExtensionFilter("All files (*.*)", "*.*")
//                fileChooser.initialFileName = fileName
//                fileChooser.extensionFilters.addAll(mainFilter, subFilter)
//                val file = fileChooser.showSaveDialog(stage)
//            }
//        }
//        contens.add(documentBox)
//    }

    val text = Text("掲載日: " + date.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
    val newsIdText = Text(newsId)
    newsIdText.isVisible = false
    val dateBox = HBox(0.0, text, newsIdText)
    contens.add(dateBox)

    val vBox = VBox(10.0)
    vBox.children.addAll(contens)
    vBox.isFillWidth = true
    return vBox
}

fun createTaskDetail(task: Task): VBox {
    val textArea = TextArea()
    textArea.text = task.getDescription()
    textArea.font = Font(14.0)
    textArea.minHeight = 150.0
    textArea.isEditable = false
    textArea.isWrapText = true

    val format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    val deadline = Text()
    deadline.text = "提出期間: " + task.getSubmissionStart().format(format) + " - " + task.getSubmissionEnd().format(format)
    val text = Text("リンク: ")
    val link = Hyperlink(task.getTaskName())
    link.setOnAction {
        val os = System.getProperty("os.name").toLowerCase()
        val url = task.getLinkUrl()
        if (os.startsWith("mac")) {
            Runtime.getRuntime().exec(arrayOf("osascript", "-e", "open location \"$url\""))
        } else if (os.startsWith("windows")) {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
        }
    }
    val vBox = VBox(10.0, textArea, deadline, VBox(0.0, text, link))
    vBox.isFillWidth = true
    return vBox
}

fun createNewsContent(jsonArray: JsonArray, style: Style): Node {
    val borderScrollPane = ScrollPane()
    val mainVBox = VBox()
    borderScrollPane.content = mainVBox
    borderScrollPane.prefWidth = 1240.0
    borderScrollPane.style = "-fx-padding: 10.0px;" + "-fx-background-color: #fff;"
    mainVBox.spacing = 5.0
    mainVBox.prefWidthProperty().bind(borderScrollPane.prefWidthProperty())
    mainVBox.style = "-fx-background-color: #fff;"
    return if (style == Style.LIST) {
        for (value in jsonArray) {
            val json = value.asObject()
            val detail = getDocument(connection, "https://king.kcg.kyoto/campus/Portal/TryAnnouncement/GetAnnouncement?aId=" + json.getInt("Id", 0))
            val newsContent = NewsContent(json, detail?.body().toString().split("\"Body\": \"")[1].split("\", \"SenderId\"")[0])
            mainVBox.children.add(newsContent)
        }
        borderScrollPane
    } else {
        val hBoxAmount = ceil(jsonArray.size() / 3.0).toInt()

        for ((index, value) in jsonArray.withIndex()) {
            val json = value.asObject()

        }

        for (i in (0 until hBoxAmount)) {

        }
        borderScrollPane
    }
}
