package com.github.kabocchi.king_LMS_Lite

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescription
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
class TaskDetail {
    
    val title: String
    val contentId: String
    val taskId: Int
    val groupId: Int
    val groupName: String
    val taskType: TaskType
    val files: JsonArray
    val taskLinkUrl: String
    
    var hasReSubmissionPeriod = false
    var hasSubmissionPeriod = false
    var submissionStart: LocalDateTime? = null
    var submissionEnd: LocalDateTime? = null
    val lastUpdate: LocalDateTime
    
    var hasDescription: Boolean = false
    var description: String = ""
    var simpleDescription: String = ""
    
    constructor(json: JsonObject, _description: String, _groupName: String, _groupId: Int, requestVerToken: String, groupAccessToken: String, userId: String) {
        title = json.getString("Title", "").trim()
        println(title)
        contentId = json.getString("ContentID", "")
        taskId = json.getInt("TaskID", 0)
        groupId = _groupId
        groupName = _groupName
    
        taskType = when (json.getInt("TaskType", 14)) {
            14 -> {
                if (json.getBoolean("IsTextReport", false)) {
                    TaskType.TEXT_REPORT
                } else {
                    TaskType.REPORT
                }
            }
            10 -> TaskType.TEST
            else -> TaskType.REPORT
        }
        
        files = json.get("backgrounds")?.asArray() ?: JsonArray()
        taskLinkUrl = "https://king.kcg.kyoto/campus/Course/$_groupId/21/#/detail/${json.getInt("TaskBlockID", 0)}/$taskId"
        
        hasReSubmissionPeriod = json.getBoolean("hasReSubmissionPeriod", false)
        hasSubmissionPeriod = json.getBoolean("hasSubmissionPeriod", false)
        lastUpdate = LocalDateTime.parse(json.getString("UpdatedDate", "").split(".")[0].removeSuffix("Z"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        if (hasReSubmissionPeriod) {
            submissionStart = LocalDateTime.parse(json.getString("SubmissionStart", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            val resubmissions = json.get("ReSubmissions").asArray()[0] as JsonObject
            submissionEnd = LocalDateTime.parse(resubmissions.getString("EndDate", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            hasReSubmissionPeriod = true
        } else if (hasSubmissionPeriod) {
            submissionStart = LocalDateTime.parse(json.getString("SubmissionStart", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            submissionEnd = LocalDateTime.parse(json.getString("SubmissionEnd", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        }
        
        description = _description.trim()
        if (description.isBlank()) {
            description = "このタスクには詳細文がありません"
            simpleDescription = ""
        } else {
            hasDescription = true
            simpleDescription = cleanDescription(description)
        }
        
    }
    
    constructor(json: JsonObject) {
        title = json.getString("Title", "")
        contentId = json.getString("ContentId", "")
        taskId = json.getInt("TaskId", 0)
        groupId = json.getInt("GroupId", 0)
        groupName = json.getString("GroupName", "")
        taskType = TaskType.valueOf(json.getString("TaskType", "REPORT"))
        
        files = json.get("Files")?.asArray() ?: JsonArray()
        taskLinkUrl = "https://king.kcg.kyoto/campus/Course/$groupId/21/#/detail/${json.getInt("TaskBlockID", 0)}/$taskId"
    
        hasSubmissionPeriod = json.getBoolean("hasSubmissionPeriod", false)
        hasReSubmissionPeriod = json.getBoolean("hasReSubmissionPeriod", false)
        lastUpdate = LocalDateTime.parse(json.getString("LastUpdate", ""))
        if (hasSubmissionPeriod) {
            submissionStart = LocalDateTime.parse(json.getString("SubmissionStart", ""))
            submissionEnd = LocalDateTime.parse(json.getString("SubmissionEnd", ""))
        }
        
        hasDescription = json.getBoolean("hasDescription", false)
        description = json.getString("Description", "")
        if (hasDescription) {
            simpleDescription = json.getString("SimpleDescription", "")
        }
    }
    
    fun toJson(): JsonObject {
        val json = JsonObject()
        json.add("Title", title)
        json.add("ContentId", contentId)
        json.add("TaskId", taskId)
        json.add("GroupId", groupId)
        json.add("GroupName", groupName)
        json.add("TaskType", taskType.name)
        json.add("Files", files)
        json.add("hasSubmissionPeriod", hasSubmissionPeriod)
        json.add("hasReSubmissionPeriod", hasReSubmissionPeriod)
        json.add("LastUpdate", lastUpdate.toString())
        if (hasSubmissionPeriod) {
            json.add("SubmissionStart", submissionStart.toString())
            json.add("SubmissionEnd", submissionEnd.toString())
        }
        json.add("hasDescription", hasDescription)
        json.add("Description", description)
        if (hasDescription) {
            json.add("SimpleDescription", simpleDescription)
        }
        return json
    }
}

