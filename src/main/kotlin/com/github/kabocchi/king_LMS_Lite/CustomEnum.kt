package com.github.kabocchi.king_LMS_Lite

enum class Category {
    NEWS,
    TASK
}

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

enum class TaskType(val typeName: String, val id: Int) {
    REPORT("レポート(ファイル提出)", 14),
    TEXT_REPORT("レポート(テキスト)", 14),
    TEST("テスト", 10)
}