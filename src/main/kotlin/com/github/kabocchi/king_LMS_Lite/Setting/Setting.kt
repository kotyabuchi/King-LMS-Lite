package com.github.kabocchi.king_LMS_Lite.Setting

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("saveFileSetting")
class Setting {
    @get:JsonProperty("notificationSetting")
    @set:JsonProperty("notificationSetting")
    @JsonProperty("notificationSetting")
    var notificationSetting: NotificationSetting = NotificationSetting()
    
    @get:JsonProperty("saveFileSetting")
    @set:JsonProperty("saveFileSetting")
    @JsonProperty("saveFileSetting")
    var saveFileSetting: SaveFileSetting = SaveFileSetting()

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
