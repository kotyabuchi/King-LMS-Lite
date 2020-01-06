package com.github.kabocchi.king_LMS_Lite.Setting

import com.fasterxml.jackson.annotation.*
import java.util.HashMap

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("day", "time")
class PopupNotificationSetting {
    @get:JsonProperty("day")
    @set:JsonProperty("day")
    @JsonProperty("day")
    var day: String? = null
    
    @get:JsonProperty("time")
    @set:JsonProperty("time")
    @JsonProperty("time")
    var time: String? = null
    
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
