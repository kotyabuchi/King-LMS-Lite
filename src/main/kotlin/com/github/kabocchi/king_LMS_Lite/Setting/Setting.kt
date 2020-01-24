package com.github.kabocchi.king_LMS_Lite.Setting

import com.fasterxml.jackson.annotation.*
import com.github.kabocchi.king_LMS_Lite.Setting.Notification.NotificationSetting
import com.github.kabocchi.king_LMS_Lite.Setting.SaveFile.SaveFileSetting
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("notificationSetting, saveFileSetting, detailCacheSetting")
class Setting {
    @get:JsonProperty("notificationSetting")
    @set:JsonProperty("notificationSetting")
    @JsonProperty("notificationSetting")
    var notificationSetting: NotificationSetting = NotificationSetting()
    
    @get:JsonProperty("saveFileSetting")
    @set:JsonProperty("saveFileSetting")
    @JsonProperty("saveFileSetting")
    var saveFileSetting: SaveFileSetting = SaveFileSetting()

    @get:JsonProperty("detailCacheSetting")
    @set:JsonProperty("detailCacheSetting")
    @JsonProperty("detailCacheSetting")
    var detailCacheSetting: DetailCacheSetting = DetailCacheSetting()

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
