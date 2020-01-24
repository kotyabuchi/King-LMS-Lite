package com.github.kabocchi.king_LMS_Lite

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.Utility.createHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils

object NewsCategory {

    private val categoryMap = mutableMapOf<Int, String>()

    init {
        createHttpClient().use { httpClient ->
            val httpGet = HttpGet("https://king.kcg.kyoto/campus/Portal/TryAnnouncement/GetCategories")
            httpClient.execute(httpGet, context).use { response ->
                val jsonArray = Json.parse(EntityUtils.toString(response.entity)).asArray()
                jsonArray.forEach {
                    it as JsonObject
                    categoryMap[it.getInt("id", 0)] = it.getString("name", "一般")
                }
            }
        }
    }

    fun getCategories(): Map<Int, String> {
        return categoryMap
    }

    fun getCategory(id: Int): String? {
        return categoryMap[id]
    }
}