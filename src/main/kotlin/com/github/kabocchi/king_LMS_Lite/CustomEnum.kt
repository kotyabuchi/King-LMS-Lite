package com.github.kabocchi.king_LMS_Lite

enum class Style {
    LIST,
    GRID
}

enum class LoginResult {
    SUCCESS,
    FAIL,
    ERROR
}

enum class ReminderType(val apiName: String) {
    E_MAIL("email"),
    POP_UP("popup")
}

enum class EventType {
    MAIN(),
    NOTICE()
}