package com.github.kabocchi.king_LMS_Lite.Node

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
    private val categoryFilterMap = mutableMapOf<NewsCategory, CheckBox>()

    private val tagFilter: FlowPane
    private val categoryFilter: FlowPane

    private val tagFilterBuffer = mutableListOf<Boolean>()
    private val categoryFilterBuffer = mutableListOf<Boolean>()

    init {
        this.apply {
            spacing = 6.0
            padding = Insets(10.0, 30.0, 10.0, 30.0)
            styleClass.addAll("news-content-box", "setting-box")
        }
        
        tagFilter = FlowPane().apply {
            vgap = 6.0
            hgap = 10.0
        }
        val tagFilterLabel = Label("タグ")
        unreadOnly = CheckBox("未読のみ")
        emergency = CheckBox("緊急のみ")
        important = CheckBox("重要のみ")
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
        NewsCategory.values().forEach {
            val categoryCheckBox = CheckBox(it.categoryName)
            categoryCheckBox.isSelected = true
            categoryFilterMap[it] = categoryCheckBox
            categoryFilter.children.add(categoryCheckBox)
            categoryFilterBuffer.add(categoryCheckBox.isSelected)
        }

        val applyBorder = BorderPane()
        val clearButton = Button("クリア").apply {
            styleClass.add("clear-button")
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
                newsPane.filterApply()
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
        val applyHBox = HBox(clearButton, applyButton).apply {
            spacing = 10.0
        }
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

    fun categoryFilter(category: NewsCategory): Boolean {
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
