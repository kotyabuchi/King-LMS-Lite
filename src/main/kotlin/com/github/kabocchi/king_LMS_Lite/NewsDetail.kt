package com.github.kabocchi.king_LMS_Lite

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.Utility.cleanDescription

class NewsDetail {

    val title: String
    val contentId: Int
    val category: String
    var isRead: Boolean
    val publishedDate: String
    var isEmergency: Boolean = false
    var isImportant: Boolean = false
    val files: JsonArray

    var hasDescription: Boolean = false
    var description: String = ""
    var simpleDescription: String = ""

    constructor(doc: String, _isRead: Boolean, _publishedDate: String) {
        val json = Json.parse(doc).asObject()
        title = json.getString("Title", "")
        contentId = json.getInt("Id", 0)
        category = NewsCategory.getCategory(json.getInt("CategoryId", 1)) ?: "一般"
        isRead = _isRead
        publishedDate = _publishedDate
        when (json.getString("Priority", "普通")) {
            "緊急" -> isEmergency = true
            "重要" -> isImportant = true
        }
        files = json.get("Files").asArray()

        description = doc.split("\"Body\": \"")[1].split("\"SenderId\":")[0].trim().removeSuffix("\",").trim()
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
         contentId = json.getInt("ContentId", 0)
         category = json.getString("Category", "一般")
         isRead = json.getBoolean("IsRead", true)
         publishedDate = json.getString("PublishedDate", "")
         isEmergency = json.getBoolean("isEmergency", false)
         isImportant = json.getBoolean("isImportant", false)
         files = json.get("Files")?.asArray() ?: JsonArray()

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
        json.add("Category", category)
        json.add("IsRead", isRead)
        json.add("PublishedDate", publishedDate)
        json.add("isEmergency", isEmergency)
        json.add("isImportant", isImportant)
        json.add("Files", files)
        json.add("hasDescription", hasDescription)
        json.add("Description", description)
        if (hasDescription) {
            json.add("SimpleDescription", simpleDescription)
        }
        return json
    }
}