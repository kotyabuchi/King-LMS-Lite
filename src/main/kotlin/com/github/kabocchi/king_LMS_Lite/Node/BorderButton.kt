package com.github.kabocchi.king_LMS_Lite.Node

import javafx.scene.control.Button

class BorderButton(text: String): Button(text) {

    init {
        this.style = """
            -fx-background-color: #30336b;
            -fx-border-color: #130f40;
            -fx-text-fill: white;
        """.trimIndent()
    }
}
