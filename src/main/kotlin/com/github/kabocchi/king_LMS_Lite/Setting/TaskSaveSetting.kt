package com.github.kabocchi.king_LMS_Lite.Setting

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("folderPath", "askingEachTime", "saveToGroupFolder")
class TaskSaveSetting {
    @get:JsonProperty("folderPath")
    @set:JsonProperty("folderPath")
    @JsonProperty("folderPath")
    var folderPath: String? = null

    @get:JsonProperty("askingEachTime")
    @set:JsonProperty("askingEachTime")
    @JsonProperty("askingEachTime")
    var askingEachTime: Boolean? = null

    @get:JsonProperty("saveToGroupFolder")
    @set:JsonProperty("saveToGroupFolder")
    @JsonProperty("saveToGroupFolder")
    var saveToGroupFolder: Boolean? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = HashMap()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }
}