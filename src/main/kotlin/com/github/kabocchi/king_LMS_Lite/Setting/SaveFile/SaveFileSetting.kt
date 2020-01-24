package com.github.kabocchi.king_LMS_Lite.Setting.SaveFile

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("news", "task")
class SaveFileSetting {
    @get:JsonProperty("news")
    @set:JsonProperty("news")
    @JsonProperty("news")
    var newsSaveSetting: NewsSaveSetting = NewsSaveSetting()

    @get:JsonProperty("task")
    @set:JsonProperty("task")
    @JsonProperty("task")
    var taskSaveSetting: TaskSaveSetting = TaskSaveSetting()

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