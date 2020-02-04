package com.github.kabocchi.king_LMS_Lite.Node.Filter

import com.github.kabocchi.kingLmsLite.Node.NewsPane
import com.github.kabocchi.king_LMS_Lite.NewsCategory
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class NewsFilterContent(newsPane: NewsPane): VBox() {

    private val unreadOnly: CheckBox
    private val emergency: CheckBox
    private val important: CheckBox
    private val categoryFilterMap = mutableMapOf<String, CheckBox>()

    private val tagFilter: FlowPane
    private val categoryFilter: FlowPane

    private val tagFilterBuffer = mutableListOf<Boolean>()
    private val categoryFilterBuffer = mutableListOf<Boolean>()

    init {
        this.apply {
            minWidth = 800.0
            spacing = 6.0
            padding = Insets(10.0, 30.0, 10.0, 30.0)
            styleClass.addAll("filter-box")
        }
        
        tagFilter = FlowPane().apply {
            vgap = 6.0
            hgap = 10.0
        }
        val tagFilterLabel = Label("タグ")
        unreadOnly = CheckBox("未読のみ")
        emergency = CheckBox("緊急")
        important = CheckBox("重要")
        tagFilter.children.addAll(unreadOnly, emergency, important)
        tagFilter.children.forEach {
            it as CheckBox
            tagFilterBuffer.add(it.isSelected)
        }

        categoryFilter = FlowPane().apply {
            vgap = 6.0
            hgap = 10.0
        }
        val categoryAllCheck = CheckBox("全てのカテゴリー").apply {
            isSelected = true
            categoryFilterBuffer.add(isSelected)
            setOnAction {
                categoryFilter.children.forEach {
                    if (it is CheckBox) {
                        Platform.runLater {
                            it.isSelected = isSelected
                        }
                    }
                }
            }
        }
        categoryFilter.children.add(categoryAllCheck)
        NewsCategory.getCategories().values.forEach {
            val categoryCheckBox = CheckBox(it)
            categoryCheckBox.isSelected = true
            categoryFilterMap[it] = categoryCheckBox
            categoryFilter.children.add(categoryCheckBox)
            categoryFilterBuffer.add(categoryCheckBox.isSelected)
        }

        val applyBorder = BorderPane()
        val clearButton = Button("クリア").apply {
            styleClass.add("border-button")
            setOnAction {
                tagFilter.children.forEach {
                    if (it is CheckBox) {
                        Platform.runLater {
                            it.isSelected = false
                        }
                    }
                }
                categoryFilter.children.forEach {
                    if (it is CheckBox) {
                        Platform.runLater {
                            it.isSelected = true
                        }
                    }
                }
            }
        }
        val applyButton = Button("適用").apply {
            styleClass.add("apply-button")
            setOnAction {
                newsPane.filterApply(true)
                // フィルターのバッファーを登録
                tagFilterBuffer.clear()
                tagFilter.children.forEach {
                    it as CheckBox
                    tagFilterBuffer.add(it.isSelected)
                }
                categoryFilterBuffer.clear()
                categoryFilter.children.forEach {
                    it as CheckBox
                    categoryFilterBuffer.add(it.isSelected)
                }
            }
        }
        val applyHBox = HBox(10.0, clearButton, applyButton)
        applyBorder.right = applyHBox

        this.children.addAll(tagFilterLabel, tagFilter, Separator(), Label("カテゴリー"), categoryFilter, Separator(), applyBorder)
    }

    fun isUnreadOnly(): Boolean {
        return unreadOnly.isSelected
    }
    
    fun showEmergency(): Boolean {
        return emergency.isSelected
    }
    
    fun showImportant(): Boolean {
        return important.isSelected
    }

    fun categoryFilter(category: String): Boolean {
        return categoryFilterMap[category]?.isSelected ?: false
    }

    fun undoFilter() {
        tagFilter.children.forEachIndexed { index, node ->
            node as CheckBox
            node.isSelected = tagFilterBuffer[index]
        }
        categoryFilter.children.forEachIndexed { index, node ->
            node as CheckBox
            node.isSelected = categoryFilterBuffer[index]
        }
    }
}
