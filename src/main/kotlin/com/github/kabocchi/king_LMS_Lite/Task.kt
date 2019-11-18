package com.github.kabocchi.king_LMS_Lite

import com.eclipsesource.json.JsonObject
import com.google.api.client.util.DateTime
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Task(json: JsonObject, description: String, groupName: String, groupId: Int) {

    private val groupName: String = groupName
    private val taskName: String = json.getString("Title", "")
    private val description: String = description
    private val linkUrl: String = "https://king.kcg.kyoto/campus/Course/$groupId/21/#/detail/${json.getInt("TaskBlockID", 0)}/${json.getInt("TaskID", 0)}"
    private val submissionStart: LocalDateTime
    private val submissionEnd: LocalDateTime
    private val lastUpdate: LocalDateTime
    private var strSubmissionStart: String = json.getString("SubmissionStart", "")
    private var strSubmissionEnd: String = json.getString("SubmissionEnd", "")
    private var strLastUpdate: String = json.getString("UpdatedDate", "")

    init {
        if (strLastUpdate.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size >= 2) strLastUpdate = strLastUpdate.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "Z"
        submissionStart = LocalDateTime.parse(strSubmissionStart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
        submissionEnd = LocalDateTime.parse(strSubmissionEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
        lastUpdate = LocalDateTime.parse(strLastUpdate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).plusHours(9)
    }
    
    internal fun getGroupName(): String {
        return groupName
    }

    internal fun getTaskName(): String {
        return taskName
    }

    internal fun getDescription(): String {
        return description
    }
    
    internal fun getLinkUrl(): String {
        return linkUrl
    }

    internal fun getSubmissionStart(): LocalDateTime {
        return submissionStart
    }

    internal fun getSubmissionEnd(): LocalDateTime {
        return submissionEnd
    }

    internal fun getLastUpdate(): LocalDateTime {
        return lastUpdate
    }
    
    internal fun toEvent(reminder: Map<ReminderType, Int>): Map<EventType, Event> {
        val startTime = DateTime(submissionStart.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
        val endTime = DateTime(submissionEnd.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
        val result = mutableMapOf<EventType, Event>()
        result[EventType.MAIN] = Event(taskName, startTime, endTime, mutableMapOf())
        result[EventType.NOTICE] = Event(taskName, startTime, endTime, reminder)
        return result
    }

    internal fun toJson(): JsonObject {
        val json = JsonObject()
        json.add("Title", taskName)
        json.add("Description", description)
        json.add("SubmissionStart", strSubmissionStart)
        json.add("SubmissionEnd", strSubmissionEnd)
        json.add("UpdatedDate", strLastUpdate)
        return json
    }
}
