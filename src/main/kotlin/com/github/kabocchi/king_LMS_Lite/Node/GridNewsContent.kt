package com.github.kabocchi.kingLmsLite.Node

import com.eclipsesource.json.JsonObject
import javafx.scene.control.Separator
import javafx.scene.layout.VBox

class GridNewsContent(json: JsonObject, description: String): VBox() {

    private val separator = Separator()

    private var showingDescription = false

    private var isRead = json.getBoolean("IsRead", false)

    private var longDescription = ""
    private var shortDescription = ""
}
