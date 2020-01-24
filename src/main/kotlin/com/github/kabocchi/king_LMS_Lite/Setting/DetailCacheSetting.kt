package com.github.kabocchi.king_LMS_Lite.Setting

import com.fasterxml.jackson.annotation.*
import java.util.HashMap

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("newsCache, taskCache, newsPath, taskPath")
class DetailCacheSetting {
    @get:JsonProperty("newsCache")
    @set:JsonProperty("newsCache")
    @JsonProperty("newsCache")
    var newsCache: Boolean? = null

    @get:JsonProperty("taskCache")
    @set:JsonProperty("taskCache")
    @JsonProperty("taskCache")
    var taskCache: Boolean? = null

    @get:JsonProperty("newsPath")
    @set:JsonProperty("newsPath")
    @JsonProperty("newsPath")
    var newsPath: String? = null

    @get:JsonProperty("taskPath")
    @set:JsonProperty("taskPath")
    @JsonProperty("taskPath")
    var taskPath: String? = null

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