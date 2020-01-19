package com.github.kabocchi.king_LMS_Lite.Node

import com.github.kabocchi.kingLmsLite.Node.TaskPane
import com.github.kabocchi.king_LMS_Lite.TaskType
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

class TaskFilterContent(taskPane: TaskPane): VBox() {

    private val tagFilter: FlowPane
    private val typeFilter: FlowPane
    private val groupFilter: FlowPane

    private val resubmissionOnly: CheckBox

    private val typeFilterMap = mutableMapOf<TaskType, CheckBox>()
    private val groupFilterMap = mutableMapOf<String, CheckBox>()

    private val tagFilterBuffer = mutableListOf<Boolean>()
    private val typeFilterBuffer = mutableListOf<Boolean>()
    private val groupFilterBuffer = mutableListOf<Boolean>()

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
        resubmissionOnly = CheckBox("再提出のみ")
        tagFilter.children.add(resubmissionOnly)
        tagFilterBuffer.add(false)

        typeFilter = FlowPane().apply {
            vgap = 6.0
            hgap = 10.0
        }
        val typeFilterLabel = Label("課題タイプ")
        val typeAllCheck = CheckBox("全てのタイプ").apply {
            isSelected = true
            typeFilterBuffer.add(true)
            setOnAction {
                typeFilter.children.forEach {
                    if (it is CheckBox) {
                        Platform.runLater {
                            it.isSelected = isSelected
                        }
                    }
                }
            }
        }
        typeFilter.children.add(typeAllCheck)
        TaskType.values().forEach {
            val typeCheckBox = CheckBox(it.typeName)
            typeCheckBox.isSelected = true
            typeFilterMap[it] = typeCheckBox
            typeFilter.children.add(typeCheckBox)
            typeFilterBuffer.add(true)
        }

        groupFilter = FlowPane().apply {
            vgap = 6.0
            hgap = 10.0
        }
        val groupFilterLabel = Label("講義名")
        val groupAllCheck = CheckBox("全ての講義").apply {
            isSelected = true
            groupFilterBuffer.add(true)
            setOnAction {
                groupFilter.children.forEach {
                    if (it is CheckBox) {
                        Platform.runLater {
                            it.isSelected = isSelected
                        }
                    }
                }
            }
        }
        groupFilter.children.add(groupAllCheck)
        taskPane.getGroupList().forEach {
            val groupCheckBox = CheckBox(it)
            groupCheckBox.isSelected = true
            groupFilterMap[it] = groupCheckBox
            groupFilter.children.add(groupCheckBox)
            groupFilterBuffer.add(true)
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
                typeFilter.children.forEach {
                    if (it is CheckBox) {
                        Platform.runLater {
                            it.isSelected = true
                        }
                    }
                }
                groupFilter.children.forEach {
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
                taskPane.filterApply(true)
                // フィルターのバッファーを登録
                tagFilterBuffer.clear()
                tagFilter.children.forEach {
                    it as CheckBox
                    tagFilterBuffer.add(it.isSelected)
                }
                typeFilterBuffer.clear()
                typeFilter.children.forEach {
                    it as CheckBox
                    typeFilterBuffer.add(it.isSelected)
                }
                groupFilterBuffer.clear()
                groupFilter.children.forEach {
                    it as CheckBox
                    groupFilterBuffer.add(it.isSelected)
                }
            }
        }
        val applyHBox = HBox(10.0, clearButton, applyButton)
        applyBorder.right = applyHBox

        this.children.addAll(tagFilterLabel, tagFilter, Separator(), typeFilterLabel, typeFilter, Separator(), groupFilterLabel, groupFilter, applyBorder)
    }

    fun undoFilter() {
        tagFilter.children.forEachIndexed { index, node ->
            node as CheckBox
            node.isSelected = tagFilterBuffer[index]
        }
        typeFilter.children.forEachIndexed { index, node ->
            node as CheckBox
            node.isSelected = typeFilterBuffer[index]
        }
        groupFilter.children.forEachIndexed { index, node ->
            node as CheckBox
            node.isSelected = groupFilterBuffer[index]
        }
    }

    fun isResubmissionOnly(): Boolean {
        return resubmissionOnly.isSelected
    }

    fun getTypeFilter(): Map<TaskType, CheckBox> {
        return typeFilterMap
    }

    fun getGroupFilter(): Map<String, CheckBox> {
        return groupFilterMap
    }
}
