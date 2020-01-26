package com.github.kabocchi.king_LMS_Lite.Node.MainPane

import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.jsoup.nodes.Document

class TimeTablePane(timeTableDoc: Document?): ScrollPane() {

    init {
        this.apply {
            isPannable = true
            isFitToWidth = true
        }

        val mainVBox = VBox(10.0).apply {
            this@TimeTablePane.content = this
            styleClass.add("time-table")
        }

        timeTableDoc?.selectFirst("table.timetable-community > tbody")?.let { tbody ->
            tbody.select("tr").forEachIndexed { index, element ->
                val timeLabel = Label(index.toString()).apply {
                    prefWidth = 16.0
                }
                val hBox = HBox(10.0, timeLabel)
                element.select("td").forEach {
                    val label = Label(it.selectFirst("span.tag-timetable")?.text() ?: "").apply {
                        isWrapText = true
                        prefWidthProperty().bind(mainVBox.widthProperty().subtract(16).multiply(0.165))
                        style = "-fx-border-width: 1px; -fx-border-color: #757575;"
                    }
                    hBox.children.add(label)
                }
                mainVBox.children.add(hBox)
            }
        }
    }
}
