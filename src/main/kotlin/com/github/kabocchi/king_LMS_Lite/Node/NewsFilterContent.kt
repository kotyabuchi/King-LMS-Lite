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
import javafx.scene.layout.VBox

class NewsFilterContent(newsPane: NewsPane): VBox() {

    private val unreadOnly: CheckBox
    private val emergency: CheckBox
    private val categoryFilterMap = mutableMapOf<NewsCategory, CheckBox>()

    init {
        this.apply {
            spacing = 6.0
            padding = Insets(10.0, 30.0, 10.0, 30.0)
            styleClass.addAll("news-content-box", "setting-box")
        }
        
        val tagFilter = FlowPane().apply {
            vgap = 6.0
            hgap = 10.0
        }
        val tagFilterText = Label("タグ")
        unreadOnly = CheckBox("未読のみ")
        emergency = CheckBox("緊急のみ")
        tagFilter.children.addAll(unreadOnly, emergency)

        val categoryFilter = FlowPane().apply {
            vgap = 6.0
            hgap = 10.0
        }
        val categoryAllCheck = CheckBox("全てのカテゴリー").apply {
            isSelected = true
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
        categoryFilter.children.add((categoryAllCheck))
        NewsCategory.values().forEach {
            val categoryCheckBox = CheckBox(it.categoryName)
            categoryCheckBox.isSelected = true
            categoryFilterMap[it] = categoryCheckBox
            categoryFilter.children.add(categoryCheckBox)
        }

        val applyBorder = BorderPane()
        val applyButton = Button("適用").apply {
            styleClass.add("apply-button")
            setOnAction {
                newsPane.filterApply()
            }
        }
        applyBorder.right = applyButton

        this.children.addAll(tagFilterText, tagFilter, Separator(), Label("カテゴリー"), categoryFilter, Separator(), applyBorder)
    }

    fun showUnreadOnly(): Boolean {
        return unreadOnly.isSelected
    }
    
    fun showEmergency(): Boolean {
        return emergency.isSelected
    }

    fun categoryFilter(category: NewsCategory): Boolean {
        return categoryFilterMap[category]?.isSelected ?: false
    }
}
