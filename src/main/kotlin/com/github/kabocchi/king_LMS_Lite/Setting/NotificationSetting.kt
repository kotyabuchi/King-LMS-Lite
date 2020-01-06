package com.github.kabocchi.king_LMS_Lite.Setting

import com.fasterxml.jackson.annotation.*
import java.util.HashMap

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("popup", "mail")
class NotificationSetting {
    @get:JsonProperty("popup")
    @set:JsonProperty("popup")
    @JsonProperty("popup")
    var popupNotificationSetting: PopupNotificationSetting = PopupNotificationSetting()
    
    @get:JsonProperty("mail")
    @set:JsonProperty("mail")
    @JsonProperty("mail")
    var mailNotificationSetting: MailNotificationSetting = MailNotificationSetting()
    
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
