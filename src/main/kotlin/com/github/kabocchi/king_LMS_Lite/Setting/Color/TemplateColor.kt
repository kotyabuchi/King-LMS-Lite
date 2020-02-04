package com.github.kabocchi.king_LMS_Lite.Setting.Color

import com.fasterxml.jackson.annotation.*
import java.util.HashMap

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("mainColor", "subColor", "textColor", "subTextColor", "linkColor")
class TemplateColor {
    @get:JsonProperty("mainColor")
    @set:JsonProperty("mainColor")
    @JsonProperty("mainColor")
    var mainColor: String? = null

    @get:JsonProperty("subColor")
    @set:JsonProperty("subColor")
    @JsonProperty("subColor")
    var subColor: String? = null

    @get:JsonProperty("textColor")
    @set:JsonProperty("textColor")
    @JsonProperty("textColor")
    var textColor: String? = null

    @get:JsonProperty("subTextColor")
    @set:JsonProperty("subTextColor")
    @JsonProperty("subTextColor")
    var subTextColor: String? = null

    @get:JsonProperty("linkColor")
    @set:JsonProperty("linkColor")
    @JsonProperty("linkColor")
    var linkColor: String? = null

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