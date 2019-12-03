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

enum class NewsCategory(val categoryName: String, val id: Int) {
    GENERAL("一般", 1),
    EVENT("イベント", 3),
    NEWS("ニュース", 5),
    CLASS_CANCELLATION("休講", 6),
    SUPPLEMENTARY_CURSE("補講", 7),
    CLASS_ROOM_CHANGE("教室変更", 8),
    STUDENT_CALL("学生呼び出し", 9),
    PERSONAL("個人", 10),
    GRADE_NOTIFICATION("成績通知", 11),
    QUESTIONNAIRE("アンケート", 12),
    PROJECT_GROUP("Project Group", 13);
}

enum class TaskType(val typeName: String, val id: Int) {
    REPORT("レポート(ファイル提出)", 14),
    TEXT_REPORT("レポート(テキスト)", 14),
    TEST("テスト", 10)
}